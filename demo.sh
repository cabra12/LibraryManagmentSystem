#!/usr/bin/env bash
# Walks through the Library API from start to finish.
# Start the API first:   docker compose up -d --build db api
# Then run this:         bash demo.sh
# The logins below are the demo ones from docker/initdb (Docker demo only).

BASE="${BASE:-http://localhost:8080}"
ADMIN="${ADMIN:-admin:DemoAdmin1!}"
MEMBER="${MEMBER:-demo@example.com:DemoMember1!}"

LAST_BODY=""
LAST_CODE=""

step() {
  echo
  echo "=== $1"
}

# call <user:password or -> <METHOD> <path> [json body]
call() {
  local creds="$1" method="$2" path="$3" body="${4:-}"
  local args=(-s -w $'\n%{http_code}' -X "$method" "$BASE$path")
  local who="no login"

  if [ "$creds" != "-" ]; then
    args+=(-u "$creds")
    who="${creds%%:*}"
  fi
  if [ -n "$body" ]; then
    args+=(-H "Content-Type: application/json" -d "$body")
  fi

  echo "> $method $path   (as $who)"
  local out
  out=$(curl "${args[@]}")
  LAST_BODY=$(echo "$out" | sed '$d')
  LAST_CODE=$(echo "$out" | tail -n 1)
  echo "$LAST_BODY"
  echo "HTTP $LAST_CODE"
}

# pulls the first "id" number out of the last JSON response
last_id() {
  echo "$LAST_BODY" | sed -E 's/.*"id":([0-9]+).*/\1/'
}

step "1. Health check (no login needed)"
call - GET /health
if [ "$LAST_CODE" != "200" ]; then
  echo
  echo "The API isn't answering at $BASE."
  echo "Start it with: docker compose up -d --build db api"
  exit 1
fi

step "2. No login: expect 401"
call - GET /books

step "3. Wrong password: expect 401"
call "admin:wrong-password" GET /books

step "4. Admin lists all books"
call "$ADMIN" GET /books

step "5. Admin adds a book: expect 201"
ISBN="978-$(date +%s)"
call "$ADMIN" POST /books "{\"title\":\"Demo Book\",\"author\":\"Demo Author\",\"isbn\":\"$ISBN\",\"totalCopies\":2}"
NEW_BOOK_ID=$(last_id)

step "6. Search by title (case-insensitive, partial)"
call "$ADMIN" GET "/books?title=dune"

step "7. Get one book by id"
call "$ADMIN" GET /books/1

step "8. Member borrows book 1: expect 201 with a due date"
call "$MEMBER" POST /books/1/borrow
LOAN_ID=$(last_id)

step "9. Member lists their own loans"
call "$MEMBER" GET /me/loans

step "10. Admin lists all active loans"
call "$ADMIN" GET /loans

step "11. Member returns the book"
if [[ "$LOAN_ID" =~ ^[0-9]+$ ]]; then
  call "$MEMBER" POST "/loans/$LOAN_ID/return"
else
  echo "Skipped: the borrow in step 8 didn't return a loan id."
fi

step "12. Member's loans again: expect an empty list"
call "$MEMBER" GET /me/loans

step "13. Admin tries to borrow: expect 403"
call "$ADMIN" POST /books/1/borrow

step "14. Member tries to add a book: expect 403"
call "$MEMBER" POST /books '{"title":"Nope","author":"Nope","isbn":"000-0","totalCopies":1}'

step "15. Admin lists overdue loans"
call "$ADMIN" GET /loans/overdue

step "16. Admin deletes the book added in step 5: expect 204"
if [[ "$NEW_BOOK_ID" =~ ^[0-9]+$ ]]; then
  call "$ADMIN" DELETE "/books/$NEW_BOOK_ID"
else
  echo "Skipped: step 5 didn't return a book id."
fi

echo
echo "Done."