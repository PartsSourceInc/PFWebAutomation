package core.practitest;

import core.practitest.model.runs.AttachmentFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface used to communicate with the PractiTest platform.
 */
public interface PractiTestApiClient {

    /**
     * Creates a test set and returns its ID
     *
     * @return ID of the newly created test set
     */
    default String createTestSetAndReturnId() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a test instance based on test set instance ID and test ID
     *
     * @param testSetInstanceId ID of the test set instance the new test instance should placed in
     * @param testId            ID of the test, on which the test instance should be based upon
     * @return ID of the newly created test instance
     */
    default String createTestInstanceAndReturnId(String testSetInstanceId, String testId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a test run
     *
     * @param testInstanceId ID of the test instance to create a test run for
     * @param testCaseSteps  List of steps, containing the test run results
     * @deprecated Deleted in lieu of submitTestRun
     */
    @Deprecated(forRemoval = true)
    default void createTestRun(String testInstanceId, List testCaseSteps) {
        throw new UnsupportedOperationException();
    }

    default TestId retrieveTestId(final TestDisplayId testDisplayId) {
        throw new UnsupportedOperationException();
    }

    default Map<TestDisplayId, TestId> retrieveTestIds(List<TestDisplayId> testDisplayIds) {
        throw new UnsupportedOperationException();
    }

    default void submitTestRun(
            TestInstanceId testInstanceId,
            List<TestStep> testSteps,
            List<Optional<String>> executionOutput,
            Optional<List<AttachmentFile>> attachments) {
        throw new UnsupportedOperationException();
    }

    default List<TestInstance> retrieveTestInstances(TestSetId testSetId) {
        throw new UnsupportedOperationException();
    }

    default List<TestInstance> retrieveTestInstances(final SetFilterId setFilterId) {
        throw new UnsupportedOperationException();
    }

    default List<TestInstance> createTestInstancesAndReturnIds(String testSetInstanceId, Collection<TestId> testIds) {
        throw new UnsupportedOperationException();
    }

    String retrieveTestPreconditions(final TestDisplayId testDisplayId);
}
