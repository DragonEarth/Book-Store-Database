import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class AddPurchase {

    private static Connection conDB;   // Connection to the database system.
    private static String url;         // URL: Which database?
    private static String user = "yamido";
    private static String cid = "test";
    private static String club;
    private static String title;
    private static String year;
    private static String whenp;
    private static Timestamp whenpST;
    private static String date;
    private static String qnty;


    public static void main(String[] args) throws ParseException {


        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-c")) {
                cid = args[i + 1];
                //System.out.print("\ncid--> " + cid);
            }

            if (args[i].equals("-b")) {
                club = args[i + 1];
                //System.out.print("\nclub--> " + club);
            }

            if (args[i].equals("-t")) {
                title = args[i + 1];
                //System.out.print("\ntitle--> " + title);
            }

            if (args[i].equals("-y")) {
                year = args[i + 1];
                //System.out.print("\nyear--> " + year);
            }

            if (args[i].equals("-w")) {
                if (args[i + 1].isEmpty()) {
                    whenp = args[i + 1];
                    whenpST = new Timestamp(System.currentTimeMillis());
                    //System.out.println("\nwhenp -->" + whenp);
                    //System.out.println("\nwhenpST -->" + whenpST);
                } else {
                    whenp = args[i + 1]; // Date string from command line
                    //System.out.println("\nwhenp -->" + whenp);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //formate we are looking for
                    try {
                        Date date = sdf.parse(whenp); //convert string to date folowing sdf formate
                        //System.out.println("\ndate -->" + date.toString() + " \ndate in millisecond -->" + date.getTime());

                        //format date object
                        whenpST = new Timestamp(date.getTime()); //convert date to millisecond then to TimeStamp
                        //System.out.println("\nConvert date to TimeStamp " + whenpST.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

            if (args[i].equals("-q")) {
                qnty = args[i + 1];

                if (Integer.parseInt(qnty) <= 0) {
                    //System.out.print("\nqnty is not a positive integer\n");
                    System.exit(0);
                }
                //System.out.print("\nqnty--> " + qnty);
            }

            if (args[i].equals("-u")) {
                user = args[i + 1];
                //System.out.print("\nuser--> " + user);
            }
        }

        // Set up the DB connection.
        try {
            // Register the driver with DriverManager.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }


        // URL: Which database?
        //url = "jdbc:postgresql://db:5432/<yamido>?currentSchema=yrb";
        url = "jdbc:postgresql://db:5432/";

        // set up acct info
        // fetch the PASSWD from <.pgpass>
        Properties props = new Properties();
        try {
            String passwd = PgPass.get("db", "*", user, user);
            props.setProperty("user", user);
            props.setProperty("password", passwd);
            // props.setProperty("ssl","true"); // NOT SUPPORTED on DB
        } catch (PgPassException e) {
            System.out.print("\nCould not obtain PASSWD from <.pgpass>.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Initialize the connection.
        try {
            //System.out.println("Connecting to database...");
            // Connect with a fall-thru id & password
            conDB = DriverManager.getConnection(url, props);
            //System.out.println("Connection established\n");
            //conDB = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            System.out.print("\nSQL: database connection error.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        //check exist of
        PreparedStatement preparedStatement = null;
        ResultSet results = null;


        //purchase was not made today
        try {
            preparedStatement = conDB.prepareStatement("SELECT whenp FROM yrb_purchase WHERE whenp= '" + whenpST + "' ;");
            //preparedStatement.setString(1, "yrb_purchase");
            results = preparedStatement.executeQuery();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(System.currentTimeMillis());
            //System.out.println(formatter.format(date));
            //System.out.println("formatter -->" + formatter.format(date));


            if (results.next()) {
                System.out.println("entry exist");
                results.close();
                preparedStatement.close();
                System.exit(0);
            }
            results.close();
            preparedStatement.close();

            if (!whenp.isEmpty() && !whenp.equals(formatter.format(date))) {
                System.out.println("purchase was not made today");
                System.exit(0);
            }
        } catch (SQLException e) {
            System.out.println("Error reading purchase table");
            System.out.println(e.toString());
            System.exit(0);
        }


        //yamido=> SELECT cid, club, title, year FROM yrb_purchase WHERE cid=7 AND club='YRB_Gold'AND title='Cats are not Dogs'AND year=1992;

        //•	The customer (cid), the club, or the book (title & year) does not exist: if it does not exist in the corresponding table,
        // the app should state this and not make any changes to the database.

        //cid
        try {
            preparedStatement = conDB.prepareStatement("SELECT cid FROM yrb_customer WHERE cid= " + cid + " ;");
            //preparedStatement.setString(1, "yrb_purchase");
            results = preparedStatement.executeQuery();
            if (!results.next()) {
                System.out.println("customer doesn't exist");
                results.close();
                preparedStatement.close();
                System.exit(0);
            }
            results.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Error accessing the table cid");
            System.out.println(e.toString());
            System.exit(0);
        }


        //club
        try {
            preparedStatement = conDB.prepareStatement("SELECT club FROM yrb_club WHERE club= '" + club + "' ;");
            //preparedStatement.setString(1, "yrb_purchase");
            results = preparedStatement.executeQuery();
            if (!results.next()) {
                System.out.println("club Doesn't exist");
                results.close();
                preparedStatement.close();
                System.exit(0);
            }
            results.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Error accessing the table club");
            System.out.println(e.toString());
            System.exit(0);
        }

        //books and year don't exist
        try {
            preparedStatement = conDB.prepareStatement("SELECT title, year FROM yrb_book WHERE title= '" + title + "' AND year=" + year + " ;");
            //preparedStatement.setString(1, "yrb_purchase");
            results = preparedStatement.executeQuery();
            if (!results.next()) {
                System.out.println("Book with this year and title doesn't exist exist ");
                results.close();
                preparedStatement.close();
                System.exit(0);
            }
            results.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("can't access the table");
            System.out.println(e.toString());
            System.exit(0);
        }

        //club don't offer the book
        //The club doesn't offer the book (title & year): if the club does not offer the book,
        // the app should state this and not make any changes to the database.
        try {
            preparedStatement = conDB.prepareStatement("SELECT club, title, year FROM yrb_offer WHERE club='" +
                    club + "' AND title= '" + title + "' AND year=" + year + " ;");
            //preparedStatement.setString(1, "yrb_purchase");
            results = preparedStatement.executeQuery();
            if (!results.next()) {
                System.out.println("club doesn't offer the book");
                results.close();
                preparedStatement.close();
                System.exit(0);
            }
            results.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Can't access the table for book offerings");
            System.out.println(e.toString());
            System.exit(0);
        }




        //Customer doesn't belong to that club
        //The customer (cid) doesn't belong to that club: if the customer is not a member of the given club,
        // the app should state this and not make any changes to the database.
        try {
            preparedStatement = conDB.prepareStatement("SELECT cid, club FROM yrb_member WHERE cid= '" + cid + "' AND club='" + club + "' ;");
            //preparedStatement.setString(1, "yrb_purchase");
            results = preparedStatement.executeQuery();
            if (!results.next()) {
                System.out.println("Customer doesn't belong to that club");
                results.close();
                preparedStatement.close();
                System.exit(0);
            }
            results.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Can't access the yrb_member");
            System.out.println(e.toString());
            System.exit(0);
        }




        // inserting data
        // inserting purchase
        preparedStatement = null;
        try {
            preparedStatement = conDB.prepareStatement("insert into yrb_purchase (cid, club, title, year, whenp, qnty) values " +
                    "(" + cid + ", '" + club + "', '" + title + "', " + year + ",'" + whenpST + "', " + qnty + ");");
            preparedStatement.executeUpdate();
            results.close();
            preparedStatement.close();
            System.out.println("purchase inserted into yrb_purchase table");
        } catch (SQLException e) {
            System.out.println("Quest: ");
            System.out.println(e.toString());
            System.exit(0);
        }


        //System.out.println(conDB);

    }

}

