package core;

import helper.Utils;
import models.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class LogConverter {

    ArrayList<Operation> convertLogs(ArrayList<String> logs) {
        ArrayList<Operation> operations = new ArrayList<>();
        for (String log : logs) {
            String statement = log.split(Pattern.quote("jdbc.sqlonly - "))[1];
            Date date = Utils.getDateFromLogString(log.split(Pattern.quote(" ["))[0]);
            String[] parts = statement.split(" ");
            long time = 0;
            if (date != null) {
                time = date.getTime();
            }
            Operation operation;
            switch (parts[0]) {
                case "INSERT":
                    operation = buildInsert(statement, time);
                    if (operation != null) operations.add(operation);
                    break;
                case "UPDATE":
                    operation = buildUpdate(statement, time);
                    if (operation != null) operations.add(operation);
                    break;
                case "DELETE":
                    operation = buildDelete(statement, time);
                    if (operation != null) operations.add(operation);
                    break;
            }

        }
        return operations;
    }

    private Operation buildInsert(String statement, long time) {
        String[] parts = statement.split(" ");
        OperationType operationType = OperationType.valueOf(parts[0]);
        ObjectType model = ObjectType.valueOf(parts[2].split(Pattern.quote("("))[0]);
        Object object = null;
        String parameters = StringUtils.substringBetween(statement, "VALUES(", ")");
        switch (model) {
            case BOOK:
                object = buildBook(parameters);
                System.out.println("book");
                break;
            case MEMBER:
                object = buildMember(parameters);
                System.out.println("member");
                break;
            case ISSUE:
                object = buildIssue(parameters, time);
                System.out.println("issue");
                break;
            case MAIL_SERVER_INFO:
                object = buildMailServerInfo(parameters);
                System.out.println("mailServerInfo");
                break;
            default: break;
        }
        if (object != null) {
            return new Operation(time, operationType, model, object);
        }
        return null;
    }

    private Operation buildUpdate(String statement, long time) {
        String[] parts = statement.split(" ");
        ObjectType model = ObjectType.valueOf(parts[1]);
        Object object = null;
        switch (model) {
            case BOOK: {
                String id = StringUtils.substringBetween(statement, "ID='", "'");
                String title = StringUtils.substringBetween(statement, "TITLE='", "'");
                String author = StringUtils.substringBetween(statement, "AUTHOR='", "'");
                String publisher = StringUtils.substringBetween(statement, "PUBLISHER='", "'");
                String isAvailable = StringUtils.substringBetween(statement, "isAvail=", " W");
                Boolean isAvailableBool = null;
                if (isAvailable != null) {
                    isAvailableBool = isAvailable.equals("true");
                }
                object = new Book(id, title, author, publisher, isAvailableBool);
                System.out.println("book");
                break;
            }
            case MEMBER: {
                String id = StringUtils.substringBetween(statement, "ID='", "'");
                String name = StringUtils.substringBetween(statement, "NAME='", "'");
                String email = StringUtils.substringBetween(statement, "EMAIL='", "'");
                String mobile = StringUtils.substringBetween(statement, "MOBILE='", "'");
                object = new Member(id, name, mobile, email);
                System.out.println("member");
                break;
            }
            case ISSUE: {
                String bookId = StringUtils.substringBetween(statement, "BOOKID='", "'");
                String renewCount = StringUtils.substringBetween(statement, "renew_count=", " W");
                object = new Issue(bookId, renewCount, time);
                System.out.println("issue");
                break;
            }
            case MAIL_SERVER_INFO: {
                // mailServerInfo is never updated
                // System.out.println("mailServerInfo");
                break;
            }
            default: break;
        }
        if (object != null) {
            return new Operation(time, OperationType.UPDATE, model, object);
        }
        return null;
    }

    private Operation buildDelete(String statement, long time) {
        String[] parts = statement.split(" ");
        ObjectType model = ObjectType.valueOf(parts[2]);
        Object object = null;
        switch (model) {
            case BOOK: {
                String id = StringUtils.substringBetween(statement, "ID='", "'");
                String title = StringUtils.substringBetween(statement, "TITLE='", "'");
                String author = StringUtils.substringBetween(statement, "AUTHOR='", "'");
                String publisher = StringUtils.substringBetween(statement, "PUBLISHER='", "'");
                object = new Book(id, title, author, publisher, null);
                System.out.println("book");
                break;
            }
            case MEMBER: {
                String id = StringUtils.substringBetween(statement, "ID='", "'");
                object = new Member(id);
                System.out.println("member");
                break;
            }
            case ISSUE: {
                //object = buildIssue(parameters);
                String bookId = StringUtils.substringBetween(statement, "BOOKID='", "'");
                object = new Issue(bookId);
                System.out.println("issue");
                break;
            }
            case MAIL_SERVER_INFO: {
                object = new MailServerInfo();
                System.out.println("mailServerInfo");
                break;
            }
            default: break;
        }
        if (object != null) {
            return new Operation(time, OperationType.DELETE, model, object);
        }
        return null;
    }

    private Book buildBook(String parameters) {
        String[] parts = removeQuotes(parameters.split(","));
        return new Book(parts[0], parts[1], parts[2], parts[3], parts[3].equals("1"));
    }

    private Member buildMember(String parameters) {
        String[] parts = removeQuotes(parameters.split(","));
        return new Member(parts[0], parts[1], parts[2], parts[3]);
    }

    private Issue buildIssue(String parameters, long time) {
        String[] parts = removeQuotes(parameters.split(","));
        return new Issue(parts[0], parts[1], "0", time);
    }

    private MailServerInfo buildMailServerInfo(String parameters) {
        String[] parts = removeQuotes(parameters.split(","));
        return new MailServerInfo(parts[0], Integer.valueOf(parts[1]), parts[2], parts[3], parts[4].equals("1"));
    }

    private String[] removeQuotes(String[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = parameters[i].replace("'", "");
        }
        return parameters;
    }

}
