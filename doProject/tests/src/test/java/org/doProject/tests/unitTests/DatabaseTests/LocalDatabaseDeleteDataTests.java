package org.doProject.tests.unitTests.DatabaseTests;

import org.doProject.infrastructure.domain.LocalDatabase;

import org.doProject.infrastructure.domain.LocalDatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class LocalDatabaseDeleteDataTests {

    private LocalDatabase localDatabase;
    private LocalDatabaseConnection localDatabaseConnection;
    private Connection mockConnection;
    private PreparedStatement mockDeleteUserStmt;
    private PreparedStatement mockDeleteProjectsStmt;
    private PreparedStatement mockDeleteTasksStmt;
    private PreparedStatement mockDeleteProjectStmt;
    private PreparedStatement mockDeleteTaskStmt;

    @BeforeEach
    public void setUp() throws SQLException {
        localDatabaseConnection = mock(LocalDatabaseConnection.class);
        localDatabase = spy(new LocalDatabase(localDatabaseConnection));;

        mockConnection = mock(Connection.class);

        mockDeleteUserStmt = mock(PreparedStatement.class);
        mockDeleteProjectsStmt = mock(PreparedStatement.class);
        mockDeleteTasksStmt = mock(PreparedStatement.class);
        mockDeleteProjectStmt = mock(PreparedStatement.class);
        mockDeleteTaskStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("DELETE FROM Users WHERE id = ?")).thenReturn(mockDeleteUserStmt);
        when(mockConnection.prepareStatement("DELETE FROM Projects WHERE userID = ?")).thenReturn(mockDeleteProjectsStmt);
        when(mockConnection.prepareStatement("DELETE FROM Tasks WHERE project_id IN (SELECT id FROM Projects WHERE userID = ?)")).thenReturn(mockDeleteTasksStmt);
        when(mockConnection.prepareStatement("DELETE FROM Tasks WHERE project_id = ?")).thenReturn(mockDeleteTasksStmt);
        when(mockConnection.prepareStatement("DELETE FROM Projects WHERE id = ?")).thenReturn(mockDeleteProjectStmt);
        when(mockConnection.prepareStatement("DELETE FROM Tasks WHERE id = ?")).thenReturn(mockDeleteTaskStmt);

        when(mockDeleteUserStmt.executeUpdate()).thenReturn(1);
        when(mockDeleteProjectsStmt.executeUpdate()).thenReturn(1);
        when(mockDeleteTasksStmt.executeUpdate()).thenReturn(1);
        when(mockDeleteProjectStmt.executeUpdate()).thenReturn(1);
        when(mockDeleteTaskStmt.executeUpdate()).thenReturn(1);

        when(localDatabaseConnection.connect()).thenReturn(mockConnection);
    }

    // USER DELETION TESTS
    @Test
    @DisplayName("Delete an existing User")
    public void testDeleteExistingUser() throws SQLException {
        int userId = 1;

        when(mockDeleteUserStmt.executeUpdate()).thenReturn(1);

        localDatabase.deleteUser(userId);

        verify(mockDeleteTasksStmt).setInt(1, userId);
        verify(mockDeleteTasksStmt).executeUpdate();

        verify(mockDeleteProjectsStmt).setInt(1, userId);
        verify(mockDeleteProjectsStmt).executeUpdate();

        verify(mockDeleteUserStmt).setInt(1, userId);
        verify(mockDeleteUserStmt).executeUpdate();
    }

    @Test
    @DisplayName("Attempt to delete a non-existing User")
    public void testDeleteNonExistingUser() throws SQLException {
        int userId = 999;

        when(mockDeleteUserStmt.executeUpdate()).thenReturn(0);

        assertThrows(SQLException.class, () -> localDatabase.deleteUser(userId));

        verify(mockDeleteTasksStmt).setInt(1, userId);
        verify(mockDeleteTasksStmt).executeUpdate();

        verify(mockDeleteProjectsStmt).setInt(1, userId);
        verify(mockDeleteProjectsStmt).executeUpdate();

        verify(mockDeleteUserStmt).setInt(1, userId);
        verify(mockDeleteUserStmt).executeUpdate();
    }


    // PROJECT DELETION TESTS
    @Test
    @DisplayName("Delete an existing project from the database and its tasks")
    public void testDeleteExistingProjectAndItsTasks() throws SQLException {
        int id = 1;

        when(mockDeleteProjectStmt.executeUpdate()).thenReturn(1);
        when(mockDeleteTasksStmt.executeUpdate()).thenReturn(2);

        localDatabase.deleteProject(id);

        // verify the queries has been executed
        // We can't forget that the deletion of project also should delete all tasks that
        // are inside this specific project
        verify(mockDeleteTasksStmt).setInt(1, id);
        verify(mockDeleteTasksStmt).executeUpdate();

        verify(mockDeleteProjectStmt).setInt(1, id);
        verify(mockDeleteProjectStmt).executeUpdate();
    }

    @Test
    @DisplayName("Attempt to delete a non-existing Project")
    public void testDeleteNonExistingProject() throws SQLException {
        int projectId = 999;

        when(mockDeleteProjectStmt.executeUpdate()).thenReturn(0);

        assertThrows(SQLException.class, () -> localDatabase.deleteProject(projectId));

        verify(mockDeleteTasksStmt).setInt(1, projectId);
        verify(mockDeleteTasksStmt).executeUpdate();

        verify(mockDeleteProjectStmt).setInt(1, projectId);
        verify(mockDeleteProjectStmt).executeUpdate();
    }


    // TASK DELETION TESTS
    @Test
    @DisplayName("Delete an existing task from database")
    public void testDeleteExistingTask() throws SQLException {
        int id = 1;

        when(mockDeleteTaskStmt.executeUpdate()).thenReturn(1);

        localDatabase.deleteTask(id);

        verify(mockDeleteTaskStmt).setInt(1, id);
        verify(mockDeleteTaskStmt).executeUpdate();
    }

    @Test
    @DisplayName("Attempt to delete a non-existing Task")
    public void testDeleteNonExistingTask() throws SQLException {
        int taskId = 999;

        when(mockDeleteTaskStmt.executeUpdate()).thenReturn(0);

        assertThrows(SQLException.class, () -> localDatabase.deleteTask(taskId));

        verify(mockDeleteTaskStmt).setInt(1, taskId);
        verify(mockDeleteTaskStmt).executeUpdate();
    }
}
