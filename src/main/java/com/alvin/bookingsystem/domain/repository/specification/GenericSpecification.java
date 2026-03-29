package com.alvin.bookingsystem.domain.repository.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericSpecification<T> {
    
    public Specification<T> getSpecification(Map<String, Object> keywordMap, List<String> fields) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (keywordMap == null || keywordMap.isEmpty() || fields.isEmpty()) {
                return null;
            }

            List<Predicate> predicates = new ArrayList<>();
            Map<String, Join<?, ?>> joins = new HashMap<>();

            for (String field : fields) {
                Object value = keywordMap.get(field);
                if (value == null) {
                    continue;
                }

                String[] parts = field.split("\\.");
                Path<?> path;

                if (parts.length == 1) {
                    path = root.get(parts[0]);
                } else {
                    Join<?, ?> currentJoin = null;
                    
                    for (int i = 0; i < parts.length - 1; i++) {
                        String joinKey = String.join(".", java.util.Arrays.copyOf(parts, i + 1));
                        
                        if (i == 0) {
                            // First level join from root
                            currentJoin = joins.computeIfAbsent(joinKey, j -> root.join(parts[0], JoinType.LEFT));
                        } else {
                            // Nested joins - get parent join
                            String parentJoinKey = String.join(".", java.util.Arrays.copyOf(parts, i));
                            Join<?, ?> parentJoin = joins.get(parentJoinKey);
                            if (parentJoin == null) {
                                throw new IllegalStateException("Parent join not found for: " + parentJoinKey);
                            }
                            int finalI = i;
                            currentJoin = joins.computeIfAbsent(joinKey, j -> parentJoin.join(parts[finalI], JoinType.LEFT));
                        }
                    }
                    
                    path = currentJoin.get(parts[parts.length - 1]);
                }

                if (value instanceof String stringValue) {
                    if (stringValue.trim().isEmpty()) {
                        continue;
                    }
                    String likePattern = "%" + stringValue.toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(path.as(String.class)), likePattern));
                } else {
                    predicates.add(cb.equal(path, value));
                }
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
