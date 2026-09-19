package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminDaoTest extends AbstractDaoTest {

    @Nested
    class AddAdminTests {

        @Test
        void insertsRowSetsGeneratedIdAndAppliesDatabaseDefaults() {
            Admin admin = new Admin("newadmin", "hash123", "New Admin");

            DaoResult result = AdminDao.addAdmin(admin);

            assertEquals(DaoResult.SUCCESS, result);
            assertTrue(admin.getId() > 0);
            Map<String, Object> row = selectRow("SELECT * FROM admins WHERE id = ?", admin.getId());
            assertNotNull(row);
            assertEquals("newadmin", row.get("username"));
            assertEquals("hash123", row.get("password"));
            assertEquals("New Admin", row.get("name"));
            // addAdmin does not insert these two columns, so the database defaults apply
            assertEquals("admin", row.get("role"));
            assertEquals(Boolean.TRUE, row.get("must_change_password"));
        }

        @Test
        void duplicateUsernameReturnsDuplicateKey() {
            insertAdmin("taken", "hash", "Existing", "admin", true);

            DaoResult result = AdminDao.addAdmin(new Admin("taken", "hash2", "Newcomer"));

            assertEquals(DaoResult.DUPLICATE_KEY, result);
            assertEquals(1, countRows("admins"));
        }
    }

    @Nested
    class LogInWithUsernameTests {

        @Test
        void existingUsernameReturnsAdminWithHashRoleAndFlag() {
            int id = insertAdmin("root", "rootHash", "Root Admin", "superadmin", false);

            Admin admin = AdminDao.logInWithUsername("root");

            assertNotNull(admin);
            assertEquals(id, admin.getId());
            assertEquals("root", admin.getUsername());
            assertEquals("rootHash", admin.getPassword());
            assertEquals("Root Admin", admin.getName());
            assertEquals("superadmin", admin.getRole());
            assertFalse(admin.getPasswordChangeStatus());
        }
    }

    @Nested
    class GetAdminByUsernameTests {

        @Test
        void existingUsernameReturnsAllFields() {
            int id = insertAdmin("helper", "helperHash", "Helper Admin", "admin", true);

            Admin admin = AdminDao.getAdminByUsername("helper");

            assertNotNull(admin);
            assertEquals(id, admin.getId());
            assertEquals("helper", admin.getUsername());
            assertEquals("helperHash", admin.getPassword());
            assertEquals("Helper Admin", admin.getName());
            assertEquals("admin", admin.getRole());
            assertTrue(admin.getPasswordChangeStatus());
        }

        @Test
        void unknownUsernameReturnsNull() {
            assertNull(AdminDao.getAdminByUsername("ghost"));
        }
    }

    @Nested
    class GetAdminByIdTests {

        @Test
        void existingIdReturnsAdminIncludingUsernameAndRole() {
            int id = insertAdmin("helper", "helperHash", "Helper Admin", "admin", true);

            Admin admin = AdminDao.getAdminById(id);

            assertNotNull(admin);
            assertEquals(id, admin.getId());
            assertEquals("helper", admin.getUsername());
            assertEquals("Helper Admin", admin.getName());
            assertEquals("admin", admin.getRole());
        }

        @Test
        void unknownIdReturnsNull() {
            assertNull(AdminDao.getAdminById(9999));
        }
    }

    @Nested
    class GetAllAdminsTests {

        @Test
        void emptyTableReturnsEmptyList() {
            List<Admin> admins = AdminDao.getAllAdmins();

            assertNotNull(admins);
            assertTrue(admins.isEmpty());
        }

        @Test
        void returnsEveryAdminWithTheirRole() {
            insertAdmin("root", "hashR", "Root Admin", "superadmin", false);
            insertAdmin("helper", "hashH", "Helper Admin", "admin", true);

            List<Admin> admins = AdminDao.getAllAdmins();

            Map<String, String> roleByUsername = admins.stream()
                    .collect(Collectors.toMap(Admin::getUsername, Admin::getRole));
            assertEquals(Map.of("root", "superadmin", "helper", "admin"), roleByUsername);
        }
    }

    @Nested
    class UpdateAdminTests {

        @Test
        void nameAndUsernameChangesArePersisted() {
            int id = insertAdmin("olduser", "hash", "Old Name", "admin", false);

            DaoResult result = AdminDao.updateAdmin(new Admin(id, "newuser", "hash", "New Name", "admin", false));

            assertEquals(DaoResult.SUCCESS, result);
            Map<String, Object> row = selectRow("SELECT * FROM admins WHERE id = ?", id);
            assertEquals("newuser", row.get("username"));
            assertEquals("New Name", row.get("name"));
            assertEquals("hash", row.get("password"));
        }

        @Test
        void passwordResetPersistsNewHashAndFlag() {
            int id = insertAdmin("helper", "oldHash", "Helper Admin", "admin", false);

            // Same change AdminSession makes when a superadmin resets an admin's password
            DaoResult result = AdminDao.updateAdmin(new Admin(id, "helper", "tempHash", "Helper Admin", "admin", true));

            assertEquals(DaoResult.SUCCESS, result);
            Map<String, Object> row = selectRow("SELECT * FROM admins WHERE id = ?", id);
            assertEquals("tempHash", row.get("password"));
            assertEquals(Boolean.TRUE, row.get("must_change_password"));
        }

        @Test
        void usernameOwnedByAnotherAdminReturnsDuplicateKeyAndLeavesRowUnchanged() {
            insertAdmin("first", "hash", "First Admin", "admin", false);
            int secondId = insertAdmin("second", "hash", "Second Admin", "admin", false);

            DaoResult result = AdminDao.updateAdmin(new Admin(secondId, "first", "hash", "Second Admin", "admin", false));

            assertEquals(DaoResult.DUPLICATE_KEY, result);
            Map<String, Object> row = selectRow("SELECT * FROM admins WHERE id = ?", secondId);
            assertEquals("second", row.get("username"));
        }
    }

    @Nested
    class DeleteAdminTests {

        @Test
        void existingAdminIsRemovedAndReturnsTrue() {
            int id = insertAdmin("helper", "hash", "Helper Admin", "admin", false);

            boolean deleted = AdminDao.deleteAdmin(id);

            assertTrue(deleted);
            assertEquals(0, countRows("admins"));
        }
    }
}

