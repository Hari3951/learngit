import java.util.ArrayList;
import java.util.Scanner;

public class MiniLibrary {
    private ArrayList<Book> inventory;

    public MiniLibrary() {
        inventory = new ArrayList<Book>();
    }

    public MiniLibrary(ArrayList<Book> startingInventory) {
        inventory = new ArrayList<Book>();
        if (startingInventory != null) {
            for (int i = 0; i < startingInventory.size(); i++) {
                Book b = startingInventory.get(i);
                if (b != null) inventory.add(b);
            }
        }
    }

    // Search by title
    public ArrayList<Book> searchByTitle(String titleQuery) {
        ArrayList<Book> results = new ArrayList<Book>();
        String q = normalize(titleQuery);

        for (int i = 0; i < inventory.size(); i++) {
            Book b = inventory.get(i);
            if (b != null) {
                String t = normalize(b.getTitle());
                if (t.indexOf(q) >= 0) {
                    results.add(b);
                }
            }
        }

        sortByRatingDesc(results);
        printResults(results);
        return results;
    }

    // Search by author
    public ArrayList<Book> searchByAuthor(String authorQuery) {
        ArrayList<Book> results = new ArrayList<Book>();
        String q = normalize(authorQuery);

        for (int i = 0; i < inventory.size(); i++) {
            Book b = inventory.get(i);
            if (b != null) {
                String a = normalize(b.getAuthor());
                if (a.indexOf(q) >= 0) {
                    results.add(b);
                }
            }
        }

        sortByRatingDesc(results);
        printResults(results);
        return results;
    }

    // Recommend books by genre.
    public ArrayList<Book> recommend(String optionalGenre) {
        ArrayList<Book> results = new ArrayList<Book>();

        String g = normalize(optionalGenre);
        boolean filterGenre = (g.length() > 0);

        for (int i = 0; i < inventory.size(); i++) {
            Book b = inventory.get(i);
            if (b != null) {
                if (!filterGenre) {
                    results.add(b);
                } else {
                    if (normalize(b.getGenre()).equals(g)) {
                        results.add(b);
                    }
                }
            }
        }

        sortByRatingDesc(results);
        printResults(results);
        return results;
    }

    public ArrayList<Book> recommend() {
        return recommend("");
    }

    // Inspect a book by title
    // Returns the Book so the user/program can call methods like rate() etc.
    public Book inspectByTitle(String exactTitle) {
        Book b = getByExactTitle(exactTitle);
        if (b == null) {
            System.out.println("No book found with title: " + exactTitle);
            return null;
        }

        System.out.println("- - Book Inspection - -");
        System.out.print(b.toDetailedString());

        // Show siblings (other books in the series)
        ArrayList<Book> siblings = b.findSiblings(this);
        if (siblings.size() > 0) {
            System.out.println("Other books in the same series:");
            sortByRatingDesc(siblings);
            for (int i = 0; i < siblings.size(); i++) {
                System.out.println("  " + siblings.get(i).toResultLine());
            }
        } else {
            if (!normalize(b.getSeries()).equals("standalone")) {
                System.out.println("No other books in the library from this series.");
            } else {
                System.out.println("This book is a Standalone (no series).");
            }
        }

        System.out.println("- - - - - - - - - - - - - - -");
        System.out.println("What you can do next in code:");
        System.out.println("  book.rate(9.5);");
        System.out.println("  library.checkout(\"" + b.getTitle() + "\");");
        System.out.println("  library.returnBook(\"" + b.getTitle() + "\");");
        System.out.println("- - - - - - - - - - - - - - -");

        return b;
    }

    public boolean add(Book book) {
        if (book == null) return false;

        // Enforce unique titles
        if (getByExactTitle(book.getTitle()) != null) {
            return false;
        }
        inventory.add(book);
        return true;
    }

    // Interactive add: asks questions, builds a Book, and adds it.
    public Book addBookInteractive(Scanner input) {
        if (input == null) return null;

        System.out.println("- - Add a Book - -");

        System.out.print("Title: ");
        String title = input.nextLine().trim();

        if (title.length() == 0) {
            System.out.println("Title cannot be blank.");
            return null;
        }
        if (getByExactTitle(title) != null) {
            System.out.println("A book with that title already exists.");
            return null;
        }

        System.out.print("Author: ");
        String author = input.nextLine().trim();

        System.out.print("Genre (Fantasy, Mystery, Romance, Historical Fiction, Horror, Nonfiction, etc.): ");
        String genre = input.nextLine().trim();

        System.out.print("Series name (or press Enter for Standalone): ");
        String series = input.nextLine().trim();
        if (series.length() == 0) series = "Standalone";

        double rating = readRating(input);

        Book created = new Book(title, author, genre, rating, series);
        inventory.add(created);

        System.out.println("Added: " + created.toResultLine());
        System.out.println("- - - - - - - - - -");

        return created;
    }

    public boolean removeByTitle(String exactTitle) {
        for (int i = 0; i < inventory.size(); i++) {
            Book b = inventory.get(i);
            if (b != null && equalsIgnoreCaseSafe(b.getTitle(), exactTitle)) {
                inventory.remove(i);
                return true;
            }
        }
        return false;
    }

    // Checkout/Return
    public boolean checkout(String exactTitle) {
        Book b = getByExactTitle(exactTitle);
        if (b == null) return false;
        if (b.isCheckedOut()) return false;
        b.setCheckedOut(true);
        return true;
    }

    public boolean returnBook(String exactTitle) {
        Book b = getByExactTitle(exactTitle);
        if (b == null) return false;
        if (!b.isCheckedOut()) return false;
        b.setCheckedOut(false);
        return true;
    }

    public ArrayList<Book> getInventory() {
        ArrayList<Book> copy = new ArrayList<Book>();
        for (int i = 0; i < inventory.size(); i++) {
            copy.add(inventory.get(i));
        }
        return copy;
    }

    public int size() { return inventory.size(); }

    public void printResults(ArrayList<Book> results) {
        System.out.println("- - Library Results - -");
        if (results == null || results.size() == 0) {
            System.out.println("(no matches)");
        } else {
            for (int i = 0; i < results.size(); i++) {
                System.out.println(results.get(i).toResultLine());
            }
        }
        System.out.println("- - - - - - - - - - - - - - -");
    }

    private Book getByExactTitle(String exactTitle) {
        for (int i = 0; i < inventory.size(); i++) {
            Book b = inventory.get(i);
            if (b != null && equalsIgnoreCaseSafe(b.getTitle(), exactTitle)) {
                return b;
            }
        }
        return null;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }

    private static boolean equalsIgnoreCaseSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private static void sortByRatingDesc(ArrayList<Book> list) {
        if (list == null) return;

        for (int i = 0; i < list.size() - 1; i++) {
            int bestIndex = i;
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).getRating() > list.get(bestIndex).getRating()) {
                    bestIndex = j;
                }
            }
            if (bestIndex != i) {
                Book temp = list.get(i);
                list.set(i, list.get(bestIndex));
                list.set(bestIndex, temp);
            }
        }
    }

    private static double readRating(Scanner input) {
       
        while (true) {
            System.out.print("Rating (0 to 10): ");
            String line = input.nextLine().trim();

            try {
                double r = Double.parseDouble(line);
                if (r >= 0.0 && r <= 10.0) {
                    return r;
                }
            } catch (Exception e) {
            }
            System.out.println("Please enter a valid number from 0 to 10.");
        }
    }

    // this is how the user would access the main class, here is an example of what I'm thinking because remember the user is a librarian and not a rgular person.
    public static void main(String[] args) {
        MiniLibrary library = new MiniLibrary();

        library.add(new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy", 9.2, "Middle-earth"));
        library.add(new Book("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", 9.5, "The Lord of the Rings"));
        library.add(new Book("The Two Towers", "J.R.R. Tolkien", "Fantasy", 9.3, "The Lord of the Rings"));
        library.add(new Book("The Return of the King", "J.R.R. Tolkien", "Fantasy", 9.6, "The Lord of the Rings"));
        library.add(new Book("Murder on the Orient Express", "Agatha Christie", "Mystery", 8.7, "Hercule Poirot"));

        Scanner sc = new Scanner(System.in);

        library.recommend("Fantasy");
        library.searchByAuthor("Tolkien");

        Book inspected = library.inspectByTitle("The Hobbit");
        if (inspected != null) {
            inspected.rate(9.8); // rating a book
        }


        boolean ok = library.checkout("The Hobbit");
        System.out.println("Checkout The Hobbit success? " + ok);

        boolean ok2 = library.returnBook("The Hobbit");
        System.out.println("Return The Hobbit success? " + ok2);

        sc.close();
    }
}
