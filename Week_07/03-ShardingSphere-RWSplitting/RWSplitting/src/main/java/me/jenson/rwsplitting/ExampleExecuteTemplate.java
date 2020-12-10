package me.jenson.rwsplitting;

import me.jenson.rwsplitting.service.ExampleService;

import java.sql.SQLException;

public class ExampleExecuteTemplate {
    public static void run(final ExampleService exampleService) throws SQLException {
        try {
            exampleService.initEnvironment();
            // write use primary ds
            exampleService.processSuccess();
            // read use secondary ds
            exampleService.printData();
        } finally {
            exampleService.cleanEnvironment();
        }
    }

    public static void runFailure(final ExampleService exampleService) throws SQLException {
        try {
            exampleService.initEnvironment();
            exampleService.processFailure();
        } finally {
            exampleService.cleanEnvironment();
        }
    }
}
