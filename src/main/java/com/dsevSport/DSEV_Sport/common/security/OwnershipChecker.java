package com.dsevSport.DSEV_Sport.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class OwnershipChecker {

    public boolean isOwnerOrAdmin(Object order, String principalName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a ->
                            "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) ||
                                    "ADMIN".equalsIgnoreCase(a.getAuthority())
                    );
            if (isAdmin) return true;
        }

        if (principalName == null || principalName.isBlank()) return false;

        try {
            try {
                var getUser = order.getClass().getMethod("getUser");
                Object user = getUser.invoke(order);
                if (user != null) {
                    for (String mName : new String[]{"getUsername", "getEmail", "getId", "getUsername"}) {
                        try {
                            var m = user.getClass().getMethod(mName);
                            Object val = m.invoke(user);
                            if (principalMatches(principalName, val)) return true;
                        } catch (NoSuchMethodException ignored) {}
                    }
                }
            } catch (NoSuchMethodException ignored) {}

            for (String mName : new String[]{"getUserId", "getCusomerId", "getCreatedBy"}) {
                try {
                    var m = order.getClass().getMethod(mName);
                    Object val = m.invoke(order);
                    if (principalMatches(principalName, val)) return true;
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean principalMatches(String principalName, Object fieldValue) {
        if (fieldValue == null) return false;
        String fv = String.valueOf(fieldValue);
        return principalName.equalsIgnoreCase(fv) || principalName.equals(fv);
    }
}
