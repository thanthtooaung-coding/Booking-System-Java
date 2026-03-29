# Manual E2E checklist (book → cancel → waitlist → promote → job)

Use this against a **running** stack: PostgreSQL, Redis, and the app (`mvn spring-boot:run` or your IDE). Default base URL: `http://localhost:8080`.

**Auth:** Protected calls need header:

`Authorization: Bearer <access_token from login>`

**JSON shape:** Responses are wrapped in `ApiResponse`; the payload is usually under **`data`** (see Swagger or network tab).

---

## 0. Preconditions

1. Apply env vars (see `README.md` / `.env.example`).
2. Start Redis: `docker compose up -d` (from project root).
3. Start the app; seed data runs on first start (countries SG/MM, packages, class schedules, etc.).
4. Open Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 1. Register → verify → login

1. **POST** `/api/users/register`  
   Body example: `{ "email": "alice@test.com", "password": "Password1!", "firstName": "Alice", "lastName": "Test", "phone": "+6500000001" }`
2. Copy **`verificationToken`** from `data` (registration response).
3. **POST** `/api/users/verify-email` — body: `{ "token": "<verificationToken>" }`
4. **POST** `/api/users/login` — body: `{ "email": "alice@test.com", "password": "Password1!" }`
5. Copy **`data.token`** from the login response — use as `Bearer` token for all steps below.

**Expect:** Login fails if email is not verified.

---

## 2. Purchase package (country-scoped)

1. **GET** `/api/packages/available?countryId=1` (adjust `countryId` if your SG country id differs; check `GET` `/api/crud/countries` or DB).
2. **POST** `/api/packages/purchase` (with Bearer)  
   Body: `{ "creditPackageId": <id> }`
3. **GET** `/api/packages/my-packages` — note **`userPackageId`** and **`remainingCredits`**.

---

## 3. Book a class

1. **GET** `/api/schedules/countries/{countryId}` — pick a **`classScheduleId`** for the same country as the package.
2. **POST** `/api/schedules/book`  
   Body: `{ "classScheduleId": <id>, "userPackageId": <id> }`
3. **GET** `/api/schedules/bookings` — confirm booking; note **`bookingId`** and credits reduced on **my-packages**.

**Expect:** Wrong country package → error. Overlapping time with another booking → error.

---

## 4. Cancel booking (4-hour refund rule)

1. **POST** `/api/schedules/bookings/{bookingId}/cancel`
2. **GET** `/api/packages/my-packages` — if cancellation is **more than 4 hours** before class start, **remaining credits** should increase by the class cost; if **within 4 hours**, credits should **not** be refunded.

**Tip:** To test refund vs no-refund, pick a schedule far in the future (refund) or temporarily adjust `class_datetime` in DB for a dev-only test (within 4h, no refund).

---

## 5. Waitlist + FIFO promotion (needs two users and a full class)

Seed schedules use **large** `max_slots` (e.g. 20). For a quick waitlist test, make **one** schedule full:

- **Option A (SQL):** `UPDATE class_schedules SET max_slots = 1, booked_slots = 0 WHERE id = <scheduleId>;` then book once as User A.
- **Option B:** Use CRUD APIs (if enabled) to set `maxSlots` to `1` on a schedule, then book until full.

**User A**

1. Register / verify / login as `userA@...`
2. Purchase SG package; **POST** `/api/schedules/book` for that schedule until `booked_slots == max_slots` (with `max_slots = 1`, one booking fills the class).

**User B**

1. Register / verify / login as `userB@...`
2. Purchase SG package (same country).
3. **POST** `/api/schedules/waitlist`  
   Body: `{ "classScheduleId": <same>, "userPackageId": <B's package> }`
4. **GET** `/api/schedules/waitlists` — entry `WAITING`, **`position`** = 1 (FIFO).

**Promotion**

1. User A: **POST** `/api/schedules/bookings/{bookingId}/cancel` (respect refund rules if you care about this step’s credits).
2. User B: **GET** `/api/schedules/bookings` — should show a **new** booking for that class (promoted from waitlist). **Credits were already deducted when B joined the waitlist** — promotion does not deduct again.

---

## 6. Waitlist refund job (after class ends)

Quartz job **`WaitlistRefundJob`** runs on a **cron** (default every **15 minutes** — see `application.properties`).

It refunds **WAITING** waitlist rows whose **class end time** is &lt; now: credits are added back to **`remainingCredits`**.

**Ways to verify:**

1. **Wait** until after the real class end + job run, then **GET** `/api/packages/my-packages` and **GET** `/api/schedules/waitlists` (status should move to **`REFUNDED`** for processed rows).
2. **Faster dev check:**  
   - Put a WAITING waitlist entry on a class **in the past** (update `class_schedules.class_datetime` and/or `class_definitions.duration_minutes` so that end time &lt; `now`, or insert test data).  
   - Wait for the next job tick or temporarily lower the cron interval in `application.properties` for local testing.

**Logs:** Watch application logs for `WaitlistRefundJob` / `WaitlistRefundJob completed`.

---

## 7. Optional: check-in

**POST** `/api/schedules/bookings/{bookingId}/check-in`

Only within the allowed window (e.g. **15 minutes before** class start until class ends — see `ScheduleServiceImpl`). Adjust `class_datetime` in DB if you need to test the window without waiting.

---

## Quick curl pattern (PowerShell)

```powershell
$base = "http://localhost:8080"
$h = @{ "Content-Type" = "application/json" }
# After login:
$token = "<paste token>"
$auth = @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" }
Invoke-RestMethod -Uri "$base/api/users/profile" -Headers $auth
```

---

## Summary

| Step | What to verify |
|------|----------------|
| Book | Credits go down; slot count increases |
| Cancel | Refund only if &gt; 4h before start |
| Waitlist | Join only when full; credits deducted at join |
| Promote | First waitlist becomes booked when a slot frees; no second credit deduction |
| Job | Still-WAITING after class end → credits refunded; status REFUNDED |
