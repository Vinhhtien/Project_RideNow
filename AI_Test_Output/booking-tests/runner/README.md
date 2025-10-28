Run instructions

1) Ensure Java 17+ and Maven are installed and on PATH.
2) From repo root, run:
   mvn -f AI_Test_Output/booking-tests/runner/pom.xml test

Notes
- Tests are standalone and do NOT touch the app’s DB.
- All dependencies are test-scoped; sources live in ../java-stubs/src/test/java.
- Uses JUnit 5, Mockito, and Jakarta Servlet 6.

