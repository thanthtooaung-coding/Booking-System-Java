package com.alvin.bookingsystem.domain.dao;

import com.alvin.bookingsystem.dto.response.AvailablePackageResponse;
import com.alvin.bookingsystem.dto.response.UserPackageDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PackageDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get available packages by country ID
     */
    public List<AvailablePackageResponse> findAvailablePackagesByCountryId(Long countryId) {
        String sql = """
            SELECT 
                cp.id,
                cp.name,
                cp.country_id,
                c.name as country_name,
                c.code as country_code,
                cp.credits,
                cp.price,
                cp.validity_days,
                cp.description,
                cp.active
            FROM credit_packages cp
            JOIN countries c ON cp.country_id = c.id
            WHERE cp.country_id = ?
              AND cp.active = true
              AND c.active = true
            ORDER BY cp.price ASC, cp.name ASC
            """;

        return jdbcTemplate.query(sql, new AvailablePackageRowMapper(), countryId);
    }

    /**
     * Get user's purchased packages
     */
    public List<UserPackageDetailResponse> findUserPackages(Long userId) {
        String sql = """
            SELECT 
                up.id,
                up.user_id,
                u.email as user_email,
                up.credit_package_id,
                cp.name as credit_package_name,
                c.id as country_id,
                c.name as country_name,
                c.code as country_code,
                cp.credits,
                up.remaining_credits,
                up.purchased_at,
                up.expires_at,
                up.status,
                up.created_by_id,
                up.updated_by_id,
                up.created_at,
                up.updated_at
            FROM user_packages up
            JOIN credit_packages cp ON up.credit_package_id = cp.id
            JOIN countries c ON cp.country_id = c.id
            JOIN users u ON up.user_id = u.id
            WHERE up.user_id = ?
            ORDER BY up.purchased_at DESC
            """;

        return jdbcTemplate.query(sql, new UserPackageDetailRowMapper(), userId);
    }

    /**
     * Get user package details by ID
     */
    public UserPackageDetailResponse findUserPackageById(Long userPackageId) {
        String sql = """
            SELECT 
                up.id,
                up.user_id,
                u.email as user_email,
                up.credit_package_id,
                cp.name as credit_package_name,
                c.id as country_id,
                c.name as country_name,
                c.code as country_code,
                cp.credits,
                up.remaining_credits,
                up.purchased_at,
                up.expires_at,
                up.status,
                up.created_by_id,
                up.updated_by_id,
                up.created_at,
                up.updated_at
            FROM user_packages up
            JOIN credit_packages cp ON up.credit_package_id = cp.id
            JOIN countries c ON cp.country_id = c.id
            JOIN users u ON up.user_id = u.id
            WHERE up.id = ?
            """;

        List<UserPackageDetailResponse> results = jdbcTemplate.query(sql, new UserPackageDetailRowMapper(), userPackageId);
        return results.isEmpty() ? null : results.get(0);
    }

    private static class AvailablePackageRowMapper implements RowMapper<AvailablePackageResponse> {
        @Override
        public AvailablePackageResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AvailablePackageResponse.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .countryId(rs.getLong("country_id"))
                    .countryName(rs.getString("country_name"))
                    .countryCode(rs.getString("country_code"))
                    .credits(rs.getInt("credits"))
                    .price(rs.getBigDecimal("price"))
                    .validityDays(rs.getInt("validity_days"))
                    .description(rs.getString("description"))
                    .active(rs.getBoolean("active"))
                    .build();
        }
    }

    private static class UserPackageDetailRowMapper implements RowMapper<UserPackageDetailResponse> {
        @Override
        public UserPackageDetailResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            java.sql.Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
            Long updatedById = rs.getLong("updated_by_id");
            if (rs.wasNull()) {
                updatedById = null;
            }

            return UserPackageDetailResponse.builder()
                    .id(rs.getLong("id"))
                    .userId(rs.getLong("user_id"))
                    .userEmail(rs.getString("user_email"))
                    .creditPackageId(rs.getLong("credit_package_id"))
                    .creditPackageName(rs.getString("credit_package_name"))
                    .countryId(rs.getLong("country_id"))
                    .countryName(rs.getString("country_name"))
                    .countryCode(rs.getString("country_code"))
                    .credits(rs.getInt("credits"))
                    .remainingCredits(rs.getInt("remaining_credits"))
                    .purchasedAt(rs.getTimestamp("purchased_at").toLocalDateTime())
                    .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
                    .status(rs.getString("status"))
                    .masterData(com.alvin.bookingsystem.dto.response.MasterData.builder()
                            .id(rs.getLong("id"))
                            .createdBy(rs.getLong("created_by_id"))
                            .updatedBy(updatedById)
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .updatedAt(updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null)
                            .build())
                    .build();
        }
    }
}
