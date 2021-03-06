package dao;

import model.Priority;
import model.Status;
import model.Ticket;
import model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;
import java.util.List;

public class TicketDaoInDbImpl implements TicketDao {
    private final Connection conn;

    public TicketDaoInDbImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void saveTicket(Ticket ticket) {
        String ticketName = ticket.getTicketName();
        String description = ticket.getDescription();
        User assignee = ticket.getAssignee();
        User reporter = ticket.getReporter();
        Status status = ticket.getStatus();
        Priority priority = ticket.getPriority();
        Calendar spentTime = ticket.getSpentTime();
        Calendar estimatedTime = ticket.getEstimatedTime();
        Ticket checkTicket = getTicketByName(ticketName);
        if (checkTicket == null) {
            String addTicketQuery =
                    String.format("insert into bug_tracker.ticket_table (ticket_name, description," +
                                    " assignee, reporter, status, priority, spentTime, estimatedTime)" +
                                    " values ('%s', '%s', %d, %d, %d, %d, '%tY-%<tm-%<td', '%tY-%<tm-%<td');",
                            ticketName, description, getIdByUser(assignee), getIdByUser(reporter),
                            getIdByStatus(status), getIdByPriority(priority),
                            spentTime.getTime(), estimatedTime.getTime());
            try (Statement stmnt = conn.createStatement()) {
                stmnt.executeUpdate(addTicketQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Ticket with this name is already registered in the database.");
        }
    }

    @Override
    public Ticket getTicketByName(String ticketName) {
        if (ticketName == null) return null;
        String ticketNameFromDB = null;
        String description = null;
        User assignee = null;
        User reporter = null;
        Status status = null;
        Priority priority = null;
        Calendar spentTime = null;
        Calendar estimatedTime = null;
        String selectTicketByNameQuery =
                String.format("select * from bug_tracker.ticket_table where ticket_name = '%s';", ticketName);
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtTicket = stmnt.executeQuery(selectTicketByNameQuery);
            while (resultSetForSoughtTicket.next()) {
                ticketNameFromDB = resultSetForSoughtTicket.getString("ticket_name");
                description = resultSetForSoughtTicket.getString("description");
                assignee = getUserById(resultSetForSoughtTicket.getInt("assignee"));
                reporter = getUserById(resultSetForSoughtTicket.getInt("reporter"));
                status = getStatusById(resultSetForSoughtTicket.getInt("status"));
                priority = getPriorityById(resultSetForSoughtTicket.getInt("priority"));
                spentTime = dateToCalendarConvertor(resultSetForSoughtTicket.getDate("spentTime"));
                estimatedTime = dateToCalendarConvertor(resultSetForSoughtTicket.getDate("estimatedTime"));
            }
            return ticketNameFromDB == null ? null :
                    new Ticket(ticketNameFromDB, description, assignee, reporter, status, priority, spentTime, estimatedTime);
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Ticket> getAll() {
        List<Ticket> tickets = new ArrayList<>();
        String ticketName;
        String description;
        User assignee;
        User reporter;
        Status status;
        Priority priority;
        Calendar spentTime;
        Calendar estimatedTime;
        String selectAllTicketsQuery = "select * from bug_tracker.ticket_table;";
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetAllTicketsFromDB = stmnt.executeQuery(selectAllTicketsQuery);
            while (resultSetAllTicketsFromDB.next()) {
                ticketName = resultSetAllTicketsFromDB.getString("ticket_name");
                description = resultSetAllTicketsFromDB.getString("description");
                assignee = getUserById(resultSetAllTicketsFromDB.getInt("assignee"));
                reporter = getUserById(resultSetAllTicketsFromDB.getInt("reporter"));
                status = getStatusById(resultSetAllTicketsFromDB.getInt("status"));
                priority = getPriorityById(resultSetAllTicketsFromDB.getInt("priority"));
                spentTime = dateToCalendarConvertor(resultSetAllTicketsFromDB.getDate("spentTime"));
                estimatedTime = dateToCalendarConvertor(resultSetAllTicketsFromDB.getDate("estimatedTime"));
                tickets.add(new Ticket(ticketName, description, assignee, reporter, status, priority, spentTime, estimatedTime));
            }
            return tickets;
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Ticket deleteTicket(Ticket ticket) {
        String ticetName = ticket.getTicketName();
        String deleteTicketByNameQuery =
                String.format("delete from bug_tracker.ticket_table where ticket_name = '%s';", ticetName);
        try (Statement stmnt = conn.createStatement()) {
            stmnt.executeUpdate(deleteTicketByNameQuery);
            return getTicketByName(ticetName);
        } catch (SQLException e) {
            e.printStackTrace();
            return getTicketByName(ticetName);
        }
    }

    @Override
    public void updateTicket(Ticket oldTicket, Ticket newTicket) {
        String oldTicketName = oldTicket.getTicketName();
        String newTicketName = newTicket.getTicketName();
        String newTicketDescription = newTicket.getDescription();
        User newTicketAssignee = newTicket.getAssignee();
        User newTicketReporter = newTicket.getReporter();
        Status newTicketStatus = newTicket.getStatus();
        Priority newTicketPriority = newTicket.getPriority();
        Calendar newTicketSpentTime = newTicket.getSpentTime();
        Calendar newTicketEstimatedTime = newTicket.getEstimatedTime();
        Ticket checkTicket = getTicketByName(newTicketName);
        if (checkTicket == null) {
            System.out.println("No ticket with this name in the database!");
        } else {
            String updateTicketQuery =
                    String.format("update ticket_table set ticket_name = '%s', description = '%s'," +
                                    " assignee = '%d', reporter = '%d', status = '%d', priority = '%d'," +
                                    " spentTime = '%tY-%<tm-%<td', estimatedTime = '%tY-%<tm-%<td' where ticket_name = '%s';",
                            newTicketName, newTicketDescription, getIdByUser(newTicketAssignee),
                            getIdByUser(newTicketReporter), getIdByStatus(newTicketStatus),
                            getIdByPriority(newTicketPriority), newTicketSpentTime.getTime(),
                            newTicketEstimatedTime.getTime(), oldTicketName);
            try (Statement stmnt = conn.createStatement()) {
                stmnt.executeUpdate(updateTicketQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private User getUserById(int userId) {
        String userNameFromDB = null;
        String userPassword = null;
        String selectUserByIdQuery =
                String.format("select * from bug_tracker.user_table where user_table.id_user = '%d';", userId);
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtUser = stmnt.executeQuery(selectUserByIdQuery);
            while (resultSetForSoughtUser.next()) {
                userNameFromDB = resultSetForSoughtUser.getString("user_name");
                userPassword = resultSetForSoughtUser.getString("password");
            }
            return userNameFromDB == null ? null : new User(userNameFromDB, userPassword);
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getIdByUser(User user) {
        int userId = 0;
        String selectIdByUserQuery =
                String.format("select id_user from bug_tracker.user_table where user_name = '%s';", user.getUserName());
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtUserId = stmnt.executeQuery(selectIdByUserQuery);
            while (resultSetForSoughtUserId.next()) {
                userId = resultSetForSoughtUserId.getInt("id_user");
            }
            return userId;
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return userId;
        }
    }

    private Status getStatusById(int statusId) {
        Status status = null;
        String selectStatusByIdQuery =
                String.format("select * from bug_tracker.status_table where id_status = '%d';", statusId);
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtStatus = stmnt.executeQuery(selectStatusByIdQuery);
            while (resultSetForSoughtStatus.next()) {
                status = Status.valueOf(resultSetForSoughtStatus.getString("status_name"));
            }
            return status;
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return status;
        }
    }

    private int getIdByStatus(Status status) {
        int statusId = 0;
        String selectIdByStatusQuery =
                String.format("select id_status from bug_tracker.status_table where status_name = '%s';", status.name());
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtStatusId = stmnt.executeQuery(selectIdByStatusQuery);
            while (resultSetForSoughtStatusId.next()) {
                statusId = resultSetForSoughtStatusId.getInt("id_status");
            }
            return statusId;
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return statusId;
        }
    }

    private Priority getPriorityById(int priorityId) {
        Priority priority = null;
        String selectPriorityByIdQuery =
                String.format("select * from bug_tracker.priority_table where priority_table.id_priority = '%d';", priorityId);
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtPriority = stmnt.executeQuery(selectPriorityByIdQuery);
            while (resultSetForSoughtPriority.next()) {
                priority = Priority.valueOf(resultSetForSoughtPriority.getString("priority_name"));
            }
            return priority;
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getIdByPriority(Priority priority) {
        int priorityId = 0;
        String selectIdByPriorityQuery =
                String.format("select id_priority from bug_tracker.priority_table where priority_name = '%s';", priority.name());
        try (Statement stmnt = conn.createStatement()) {
            ResultSet resultSetForSoughtPriorityId = stmnt.executeQuery(selectIdByPriorityQuery);
            while (resultSetForSoughtPriorityId.next()) {
                priorityId = resultSetForSoughtPriorityId.getInt("id_priority");
            }
            return priorityId;
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return priorityId;
        }
    }

    private Calendar dateToCalendarConvertor(Date dateFromDB) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFromDB);
        return calendar;
    }
}
