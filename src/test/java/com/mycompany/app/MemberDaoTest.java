package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberDaoTest extends AbstractDaoTest {

    @Nested
    class AddMemberTests {

        @Test
        void insertsRowAndSetsGeneratedId() {
            Member member = new Member("Jane Doe", "jane@example.com", "hash123", true);

            DaoResult result = MemberDao.addMember(member);

            assertEquals(DaoResult.SUCCESS, result);
            assertTrue(member.getId() > 0);
            Map<String, Object> row = selectRow("SELECT * FROM members WHERE id = ?", member.getId());
            assertNotNull(row);
            assertEquals("Jane Doe", row.get("name"));
            assertEquals("jane@example.com", row.get("email"));
            assertEquals("hash123", row.get("password"));
            assertEquals(Boolean.TRUE, row.get("must_change_password"));
        }

        @Test
        void duplicateEmailReturnsDuplicateKey() {
            insertMember("Existing", "taken@example.com", "hash", false);

            DaoResult result = MemberDao.addMember(new Member("Newcomer", "taken@example.com", "hash2", false));

            assertEquals(DaoResult.DUPLICATE_KEY, result);
            assertEquals(1, countRows("members"));
        }

        @Test
        void nullNameViolatesNotNullAndReturnsDatabaseError() {
            DaoResult result = MemberDao.addMember(new Member(null, "noname@example.com", "hash", false));

            assertEquals(DaoResult.DATABASE_ERROR, result);
            assertEquals(0, countRows("members"));
        }
    }

    @Nested
    class GetAllMembersTests {

        @Test
        void emptyTableReturnsEmptyList() {
            List<Member> members = MemberDao.getAllMembers();

            assertNotNull(members);
            assertTrue(members.isEmpty());
        }

        @Test
        void returnsEveryMemberWithoutPasswords() {
            insertMember("Alice", "alice@example.com", "hashA", false);
            insertMember("Bob", "bob@example.com", "hashB", true);

            List<Member> members = MemberDao.getAllMembers();

            assertEquals(2, members.size());
            Set<String> emails = members.stream().map(Member::getEmail).collect(Collectors.toSet());
            assertEquals(Set.of("alice@example.com", "bob@example.com"), emails);
            members.forEach(m -> assertNull(m.getPassword()));
        }
    }

    @Nested
    class GetMemberIdTests {

        @Test
        void existingIdReturnsMember() {
            int id = insertMember("Jane Doe", "jane@example.com", "hash", true);

            Member member = MemberDao.getMemberId(id);

            assertNotNull(member);
            assertEquals(id, member.getId());
            assertEquals("Jane Doe", member.getName());
            assertEquals("jane@example.com", member.getEmail());
            assertTrue(member.getPasswordChangeStatus());
        }

        @Test
        void unknownIdReturnsNull() {
            assertNull(MemberDao.getMemberId(9999));
        }
    }

    @Nested
    class GetMemberByEmailTests {

        @Test
        void existingEmailReturnsMemberIncludingPasswordHash() {
            int id = insertMember("Jane Doe", "jane@example.com", "storedHash", true);

            Member member = MemberDao.getMemberByEmail("jane@example.com");

            assertNotNull(member);
            assertEquals(id, member.getId());
            assertEquals("storedHash", member.getPassword());
            assertTrue(member.getPasswordChangeStatus());
        }

        @Test
        void unknownEmailReturnsNull() {
            assertNull(MemberDao.getMemberByEmail("nobody@example.com"));
        }
    }

    @Nested
    class SearchByNameTests {

        @Test
        void partialCaseInsensitiveMatchReturnsOnlyMatches() {
            insertMember("Jane Doe", "jane@example.com", "hash", false);
            insertMember("Bob Stone", "bob@example.com", "hash", false);
            insertMember("JANET Lee", "janet@example.com", "hash", false);

            List<Member> results = MemberDao.searchByName("jan");

            Set<String> names = results.stream().map(Member::getName).collect(Collectors.toSet());
            assertEquals(Set.of("Jane Doe", "JANET Lee"), names);
        }

        @Test
        void noMatchReturnsEmptyList() {
            insertMember("Jane Doe", "jane@example.com", "hash", false);

            List<Member> results = MemberDao.searchByName("zzz");

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class UpdateMemberTests {

        @Test
        void nameAndEmailChangesArePersisted() {
            int id = insertMember("Old Name", "old@example.com", "hash", false);

            DaoResult result = MemberDao.updateMember(new Member(id, "New Name", "new@example.com", false));

            assertEquals(DaoResult.SUCCESS, result);
            Map<String, Object> row = selectRow("SELECT * FROM members WHERE id = ?", id);
            assertEquals("New Name", row.get("name"));
            assertEquals("new@example.com", row.get("email"));
        }

        @Test
        void emailOwnedByAnotherMemberReturnsDuplicateKeyAndLeavesRowUnchanged() {
            insertMember("Alice", "alice@example.com", "hash", false);
            int bobId = insertMember("Bob", "bob@example.com", "hash", false);

            DaoResult result = MemberDao.updateMember(new Member(bobId, "Bob", "alice@example.com", false));

            assertEquals(DaoResult.DUPLICATE_KEY, result);
            Map<String, Object> row = selectRow("SELECT * FROM members WHERE id = ?", bobId);
            assertEquals("bob@example.com", row.get("email"));
        }

        @Test
        void newPasswordHashAndFlagArePersisted() {
            int id = insertMember("Jane Doe", "jane@example.com", "oldHash", true);

            DaoResult result = MemberDao.updateMember(new Member(id, "Jane Doe", "jane@example.com", "newHash", false));

            assertEquals(DaoResult.SUCCESS, result);
            Map<String, Object> row = selectRow("SELECT * FROM members WHERE id = ?", id);
            assertEquals("newHash", row.get("password"));
            assertEquals(Boolean.FALSE, row.get("must_change_password"));
        }

        @Test
        void memberWithoutPasswordKeepsStoredPassword() {
            int id = insertMember("Jane Doe", "jane@example.com", "keepMe", false);

            // Same constructor the search methods use: password is null
            DaoResult result = MemberDao.updateMember(new Member(id, "Jane Renamed", "jane@example.com", false));

            assertEquals(DaoResult.SUCCESS, result);
            Map<String, Object> row = selectRow("SELECT * FROM members WHERE id = ?", id);
            assertEquals("Jane Renamed", row.get("name"));
            assertEquals("keepMe", row.get("password"));
        }
    }

    @Nested
    class DeleteMemberTests {

        @Test
        void memberWithNoLoansIsDeleted() {
            int id = insertMember("Jane Doe", "jane@example.com", "hash", false);

            MemberDao.deleteMember(id);

            assertEquals(0, countRows("members"));
        }

        @Test
        void memberWithALoanIsBlockedByForeignKey() {
            int memberId = insertMember("Jane Doe", "jane@example.com", "hash", false);
            int bookId = insertBook("Dune", "Frank Herbert", "978-0441013593", 1, 0);
            insertBorrowedBook(bookId, memberId);

            MemberDao.deleteMember(memberId);

            assertEquals(1, countRows("members"));
            assertEquals(1, countRows("borrowed_books"));
        }
    }
}
