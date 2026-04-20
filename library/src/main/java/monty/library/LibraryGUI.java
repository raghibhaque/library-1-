package monty.library;

import monty.library.kyle.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryGUI extends JFrame {

    // Palette
    private static final Color BG        = new Color(0xF7F6F3);
    private static final Color SURFACE   = Color.WHITE;
    private static final Color ACCENT    = new Color(0x2D6A4F);
    private static final Color ACCENT_HV = new Color(0x1B4332);
    private static final Color MUTED     = new Color(0x9E9E9E);
    private static final Color ROW_ALT   = new Color(0xF0EFE9);
    private static final Color TEXT      = new Color(0x1A1A1A);
    private static final Color BORDER    = new Color(0xE0DED8);

    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_HEAD  = new Font("Segoe UI", Font.BOLD,  11);

    private final List<Book> books = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JLabel countLabel = new JLabel();

    private final JTextField titleField  = field("Title");
    private final JTextField authorField = field("Author");
    private final JTextField yearField   = field("Year");
    private final JTextField isbnField   = field("ISBN");

    public LibraryGUI() {
        setTitle("Library");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(root);

        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Library");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT);

        countLabel.setFont(FONT_SMALL);
        countLabel.setForeground(MUTED);

        header.add(titleLabel,  BorderLayout.WEST);
        header.add(countLabel,  BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────
        String[] columns = {"Title", "Author", "Year", "ISBN"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setSelectionBackground(new Color(0xD8F3DC));
        table.setSelectionForeground(TEXT);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);

        // Header style
        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_HEAD);
        th.setForeground(MUTED);
        th.setBackground(SURFACE);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        th.setReorderingAllowed(false);

        // Alternating rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!sel) setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(SURFACE);
        root.add(scroll, BorderLayout.CENTER);

        // ── Form ─────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel inputs = new JPanel(new GridLayout(1, 4, 10, 0));
        inputs.setOpaque(false);
        inputs.add(titleField);
        inputs.add(authorField);
        inputs.add(yearField);
        inputs.add(isbnField);
        bottom.add(inputs, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton removeBtn = ghostButton("Remove");
        JButton addBtn    = accentButton("+ Add Book");
        actions.add(removeBtn);
        actions.add(addBtn);
        bottom.add(actions, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        // ── Seed data ────────────────────────────────────────────
        seed();

        // ── Listeners ────────────────────────────────────────────
        addBtn.addActionListener(e -> {
            try {
                String t = titleField.getText().trim();
                String a = authorField.getText().trim();
                if (t.isEmpty() || a.isEmpty()) { shake(titleField.getText().isEmpty() ? titleField : authorField); return; }
                int    yr   = Integer.parseInt(yearField.getText().trim());
                double isbn = Double.parseDouble(isbnField.getText().trim());
                addBook(new Book(t, a, yr, isbn));
                clearForm();
            } catch (NumberFormatException ex) {
                shake(yearField.getText().trim().isEmpty() || !isDigits(yearField.getText()) ? yearField : isbnField);
            }
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            books.remove(row);
            tableModel.removeRow(row);
            updateCount();
        });

        setSize(780, 540);
        setMinimumSize(new Dimension(680, 420));
        setLocationRelativeTo(null);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void seed() {
        addBook(new Book("The Hobbit",                    "J.R.R. Tolkien",        1937, 9780547928227.0));
        addBook(new Book("1984",                          "George Orwell",         1949, 9780451524935.0));
        addBook(new Book("To Kill a Mockingbird",         "Harper Lee",            1960, 9780061743528.0));
        addBook(new Book("The Great Gatsby",              "F. Scott Fitzgerald",   1925, 9780743273565.0));
        addBook(new Book("Brave New World",               "Aldous Huxley",         1932, 9780060850524.0));
        addBook(new Book("The Catcher in the Rye",        "J.D. Salinger",         1951, 9780316769174.0));
        addBook(new Book("Dune",                          "Frank Herbert",         1965, 9780441013593.0));
        addBook(new Book("Fahrenheit 451",                "Ray Bradbury",          1953, 9781451673319.0));
        addBook(new Book("The Name of the Wind",          "Patrick Rothfuss",      2007, 9780756404741.0));
        addBook(new Book("Clean Code",                    "Robert C. Martin",      2008, 9780132350884.0));
        addBook(new Book("The Pragmatic Programmer",      "David Thomas",          1999, 9780135957059.0));
        addBook(new Book("Sapiens",                       "Yuval Noah Harari",     2011, 9780062316097.0));
        addBook(new Book("Thinking, Fast and Slow",       "Daniel Kahneman",       2011, 9780374533557.0));
        addBook(new Book("The Alchemist",                 "Paulo Coelho",          1988, 9780062315007.0));
        addBook(new Book("Crime and Punishment",          "Fyodor Dostoevsky",     1866, 9780143107637.0));
    }

    private void addBook(Book book) {
        books.add(book);
        tableModel.addRow(new Object[]{book.getTitle(), book.getAuthor(), book.getYearofPublication(), (long) book.getIsbn()});
        updateCount();
    }

    private void updateCount() {
        countLabel.setText(books.size() + (books.size() == 1 ? " book" : " books"));
    }

    private void clearForm() {
        titleField.setText("");  titleField.putClientProperty("hint", true);
        authorField.setText(""); authorField.putClientProperty("hint", true);
        yearField.setText("");   yearField.putClientProperty("hint", true);
        isbnField.setText("");   isbnField.putClientProperty("hint", true);
        repaint();
    }

    private static JTextField field(String hint) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(FONT_SMALL);
                    Insets ins = getInsets();
                    g2.drawString(hint, ins.left + 2, getHeight() / 2 + 4);
                }
            }
        };
        f.setFont(FONT_BODY);
        f.setForeground(TEXT);
        f.setBackground(SURFACE);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    private static JButton accentButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HV : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(MUTED);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(7, 14, 7, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(TEXT); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(MUTED); }
        });
        return b;
    }

    private static void shake(Component c) {
        Point orig = c.getLocation();
        Timer t = new Timer(30, null);
        int[] seq = {-6, 6, -4, 4, -2, 2, 0};
        int[] idx = {0};
        t.addActionListener(e -> {
            if (idx[0] >= seq.length) { t.stop(); c.setLocation(orig); return; }
            c.setLocation(orig.x + seq[idx[0]++], orig.y);
        });
        t.start();
        c.requestFocus();
    }

    private static boolean isDigits(String s) {
        return s.matches("\\d+");
    }
}
