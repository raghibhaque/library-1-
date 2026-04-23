package monty.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import monty.library.kyle.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryGUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────
    private static final Color BG        = new Color(0x111113);
    private static final Color SURFACE   = new Color(0x1C1C1E);
    private static final Color CARD      = new Color(0x2C2C2E);
    private static final Color SEP       = new Color(0x38383A);
    private static final Color TEXT      = new Color(0xF5F5F7);
    private static final Color MUTED     = new Color(0x86868B);
    private static final Color ACCENT    = new Color(0x0A84FF);
    private static final Color ACCENTH   = new Color(0x409CFF);
    private static final Color DANGER    = new Color(0xFF453A);
    private static final Color SEL_BG    = new Color(0x09213A);
    private static final Color SEL_FG    = new Color(0x60AEFF);
    private static final Color ROW_SEP   = new Color(0x26262A);
    private static final Color AVAIL_BG  = new Color(0x0D2D1C);
    private static final Color AVAIL_FG  = new Color(0x30D158);
    private static final Color BORROW_BG = new Color(0x2E1F00);
    private static final Color BORROW_FG = new Color(0xFF9F0A);

    private static final Font F_TITLE = sysFont(24, Font.BOLD);
    private static final Font F_BODY  = sysFont(13, Font.PLAIN);
    private static final Font F_SMALL = sysFont(11, Font.PLAIN);
    private static final Font F_HEAD  = sysFont(10, Font.BOLD);

    // ── Persistence ──────────────────────────────────────────────────
    private static final File DATA_FILE =
        new File(System.getProperty("user.home"), ".library-data.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── State ────────────────────────────────────────────────────────
    private final List<Book> books = new ArrayList<>();
    private final DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private final JLabel countLbl = new JLabel();
    private JTable table;
    private JButton borrowBtn;
    private JTextField searchBar;

    private final JTextField fTitle  = inputField("Title");
    private final JTextField fAuthor = inputField("Author");
    private final JTextField fYear   = inputField("Year");
    private final JTextField fIsbn   = inputField("ISBN");
    private final JTextField fGenre  = inputField("Genre");

    public LibraryGUI() {
        setTitle("Library");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(root);

        // ── Header ──────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));

        JLabel titleLbl = new JLabel("Library");
        titleLbl.setFont(F_TITLE);
        titleLbl.setForeground(TEXT);

        searchBar = makeSearchField();
        countLbl.setFont(F_SMALL);
        countLbl.setForeground(MUTED);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);
        headerRight.add(searchBar);
        headerRight.add(countLbl);

        header.add(titleLbl,    BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────
        String[] cols = {"Title", "Author", "Year", "Genre", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = buildTable();
        sorter = new TableRowSorter<>(model);
        sorter.setSortable(4, false);
        table.setRowSorter(sorter);
        root.add(buildScroll(table), BorderLayout.CENTER);

        // ── Bottom form ──────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel divider = new JPanel();
        divider.setBackground(SEP);
        divider.setPreferredSize(new Dimension(0, 1));
        bottom.add(divider, BorderLayout.NORTH);

        JPanel inputs = new JPanel(new GridLayout(1, 5, 10, 0));
        inputs.setOpaque(false);
        inputs.setBorder(new EmptyBorder(14, 0, 0, 0));
        inputs.add(fTitle); inputs.add(fAuthor);
        inputs.add(fYear);  inputs.add(fIsbn); inputs.add(fGenre);
        bottom.add(inputs, BorderLayout.CENTER);

        JButton removeBtn = ghostBtn("Remove");
        borrowBtn = secondaryBtn("Borrow");
        borrowBtn.setEnabled(false);
        JButton addBtn = solidBtn("Add Book");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(borrowBtn);
        btns.add(removeBtn);
        btns.add(addBtn);
        bottom.add(btns, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        // ── Wire events ──────────────────────────────────────────────

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(searchBar.getText()); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(searchBar.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(searchBar.getText()); }
        });

        // Ctrl/Cmd+F → focus search
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().registerKeyboardAction(e -> searchBar.requestFocus(),
            ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Escape in search → clear and return focus
        searchBar.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        searchBar.getActionMap().put("clearSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                searchBar.setText("");
                table.requestFocus();
            }
        });

        // Double-click row → edit dialog
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int view = table.getSelectedRow();
                if (view < 0) return;
                int mi = table.convertRowIndexToModel(view);
                showEdit(books.get(mi), mi);
            }
        });

        // Row selection → update borrow button
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int view = table.getSelectedRow();
            if (view < 0) { borrowBtn.setEnabled(false); return; }
            Book b = books.get(table.convertRowIndexToModel(view));
            borrowBtn.setEnabled(true);
            borrowBtn.setText(b.isBorrowed() ? "Return" : "Borrow");
        });

        // Delete key → remove selected row
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
        table.getActionMap().put("deleteRow", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { removeSelected(); }
        });

        addBtn.addActionListener(e -> {
            try {
                String t = fTitle.getText().trim(), a = fAuthor.getText().trim();
                if (t.isEmpty() || a.isEmpty()) { shake(t.isEmpty() ? fTitle : fAuthor); return; }
                int yr      = Integer.parseInt(fYear.getText().trim());
                double isbn = Double.parseDouble(fIsbn.getText().trim());
                addBook(new Book(t, a, yr, isbn, "", fGenre.getText().trim()));
                clearForm();
                saveData();
            } catch (NumberFormatException ex) {
                shake(fYear.getText().trim().isEmpty() ? fYear : fIsbn);
            }
        });

        removeBtn.addActionListener(e -> removeSelected());

        borrowBtn.addActionListener(e -> {
            int view = table.getSelectedRow();
            if (view < 0) return;
            int mi = table.convertRowIndexToModel(view);
            Book b = books.get(mi);
            b.setBorrowed(!b.isBorrowed());
            model.setValueAt(b.isBorrowed() ? "Borrowed" : "Available", mi, 4);
            borrowBtn.setText(b.isBorrowed() ? "Return" : "Borrow");
            updateCount();
            saveData();
        });

        loadOrSeed();

        setSize(940, 620);
        setMinimumSize(new Dimension(780, 500));
        setLocationRelativeTo(null);
    }

    // ── Edit dialog ──────────────────────────────────────────────────

    private void showEdit(Book book, int modelIndex) {
        JDialog d = new JDialog(this, true);
        d.setUndecorated(true);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(SURFACE);
        main.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SEP),
            new EmptyBorder(24, 28, 24, 28)
        ));

        JLabel hdr = new JLabel("Edit Book");
        hdr.setFont(sysFont(16, Font.BOLD));
        hdr.setForeground(TEXT);

        JButton xBtn = makeXButton();
        xBtn.addActionListener(ev -> d.dispose());

        JPanel hdrRow = new JPanel(new BorderLayout());
        hdrRow.setOpaque(false);
        hdrRow.add(hdr, BorderLayout.WEST);
        hdrRow.add(xBtn, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(SEP);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(hdrRow);
        topPanel.add(Box.createVerticalStrut(14));
        topPanel.add(sep);

        JTextField eTitleF  = inputField("Title");  eTitleF.setText(book.getTitle());
        JTextField eAuthorF = inputField("Author"); eAuthorF.setText(book.getAuthor());
        JTextField eYearF   = inputField("Year");   eYearF.setText(String.valueOf(book.getYearofPublication()));
        JTextField eIsbnF   = inputField("ISBN");   eIsbnF.setText(String.valueOf((long) book.getIsbn()));
        JTextField eGenreF  = inputField("Genre");  eGenreF.setText(book.getGenre());

        JTextArea eDescA = new JTextArea(book.getDescription(), 5, 30);
        eDescA.setFont(F_BODY);
        eDescA.setForeground(new Color(0xBEBEC6));
        eDescA.setBackground(CARD);
        eDescA.setCaretColor(ACCENT);
        eDescA.setLineWrap(true);
        eDescA.setWrapStyleWord(true);
        eDescA.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane descScroll = new JScrollPane(eDescA,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descScroll.setBorder(BorderFactory.createLineBorder(SEP));
        descScroll.getViewport().setBackground(CARD);
        styleScrollBar(descScroll.getVerticalScrollBar());

        JPanel metaRow = new JPanel(new GridLayout(1, 3, 10, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaRow.add(eYearF); metaRow.add(eIsbnF); metaRow.add(eGenreF);

        JLabel descLbl = new JLabel("Description");
        descLbl.setFont(F_HEAD);
        descLbl.setForeground(MUTED);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (JTextField f : new JTextField[]{eTitleF, eAuthorF}) {
            f.setAlignmentX(Component.LEFT_ALIGNMENT);
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        formPanel.add(eTitleF);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(eAuthorF);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(metaRow);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(descLbl);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(descScroll);

        JButton cancelBtn = linkBtn("Cancel");
        JButton saveBtn   = solidBtn("Save");

        saveBtn.addActionListener(ev -> {
            String t = eTitleF.getText().trim(), a = eAuthorF.getText().trim();
            if (t.isEmpty() || a.isEmpty()) { shake(t.isEmpty() ? eTitleF : eAuthorF); return; }
            try {
                int    yr   = Integer.parseInt(eYearF.getText().trim());
                double isbn = Double.parseDouble(eIsbnF.getText().trim());
                book.setTitle(t);
                book.setAuthor(a);
                book.setYearofPublication(yr);
                book.setIsbn(isbn);
                book.setGenre(eGenreF.getText().trim());
                book.setDescription(eDescA.getText());
                model.setValueAt(t,               modelIndex, 0);
                model.setValueAt(a,               modelIndex, 1);
                model.setValueAt(yr,              modelIndex, 2);
                model.setValueAt(book.getGenre(), modelIndex, 3);
                saveData();
                d.dispose();
            } catch (NumberFormatException ex) {
                shake(eYearF.getText().trim().isEmpty() ? eYearF : eIsbnF);
            }
        });
        cancelBtn.addActionListener(ev -> d.dispose());
        d.getRootPane().registerKeyboardAction(ev -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        main.add(topPanel,  BorderLayout.NORTH);
        main.add(formPanel, BorderLayout.CENTER);
        main.add(btnRow,    BorderLayout.SOUTH);

        d.setContentPane(main);
        d.setSize(500, 460);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // ── Table ────────────────────────────────────────────────────────

    private JTable buildTable() {
        JTable t = new JTable(model);
        t.setFont(F_BODY);
        t.setForeground(TEXT);
        t.setBackground(SURFACE);
        t.setSelectionBackground(SEL_BG);
        t.setSelectionForeground(SEL_FG);
        t.setRowHeight(42);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFocusable(true);

        t.getColumnModel().getColumn(0).setPreferredWidth(220);
        t.getColumnModel().getColumn(1).setPreferredWidth(160);
        t.getColumnModel().getColumn(2).setPreferredWidth(55);
        t.getColumnModel().getColumn(3).setPreferredWidth(115);
        t.getColumnModel().getColumn(4).setPreferredWidth(110);

        t.getColumnModel().getColumn(4).setCellRenderer(new BadgeRenderer());

        JTableHeader th = t.getTableHeader();
        th.setBackground(SURFACE);
        th.setBorder(null);
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean s, boolean f, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, s, f, row, col);
                l.setFont(F_HEAD);
                l.setForeground(MUTED);
                l.setBackground(SURFACE);
                l.setOpaque(true);
                l.setText(val != null ? val.toString().toUpperCase() : "");
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, SEP),
                    new EmptyBorder(0, 14, 0, 14)
                ));
                return l;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setFont(F_BODY);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEP),
                    new EmptyBorder(0, 14, 0, 14)
                ));
                if (sel) {
                    setBackground(SEL_BG);
                    setForeground(col >= 2 ? new Color(0x4E8FC7) : SEL_FG);
                } else {
                    setBackground(SURFACE);
                    setForeground(col >= 2 ? MUTED : TEXT);
                }
                return this;
            }
        });
        return t;
    }

    // ── Status badge renderer ─────────────────────────────────────────

    private static class BadgeRenderer extends DefaultTableCellRenderer {
        private boolean borrowed;

        @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
            borrowed = "Borrowed".equals(val);
            setText("");
            setBackground(sel ? SEL_BG : SURFACE);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEP));
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String label = borrowed ? "Borrowed" : "Available";
            Color  fg    = borrowed ? BORROW_FG  : AVAIL_FG;
            Color  bg    = borrowed ? BORROW_BG  : AVAIL_BG;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(F_SMALL);

            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(label);
            int bw = tw + 18, bh = 20;
            int bx = (getWidth() - bw) / 2;
            int by = (getHeight() - bh) / 2;

            g2.setColor(bg);
            g2.fillRoundRect(bx, by, bw, bh, bh, bh);
            g2.setColor(fg);
            g2.drawString(label, bx + (bw - tw) / 2,
                by + (bh + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ── Scroll ───────────────────────────────────────────────────────

    private JScrollPane buildScroll(JTable tbl) {
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(SEP));
        sp.getViewport().setBackground(SURFACE);
        sp.setBackground(SURFACE);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(sp.getVerticalScrollBar());
        return sp;
    }

    private static void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x48484A); trackColor = SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return tinyBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return tinyBtn(); }
            private JButton tinyBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
    }

    // ── Persistence ──────────────────────────────────────────────────

    private void loadOrSeed() {
        if (DATA_FILE.exists()) {
            try {
                List<Book> loaded = MAPPER.readValue(DATA_FILE,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, Book.class));
                for (Book b : loaded) addBook(b);
                return;
            } catch (Exception ignored) {}
        }
        seed();
    }

    private void saveData() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(DATA_FILE, books);
        } catch (Exception ignored) {}
    }

    // ── Seed ─────────────────────────────────────────────────────────

    private void seed() {
        addBook(new Book("The Hobbit", "J.R.R. Tolkien", 1937, 9780547928227L,
            "Bilbo Baggins, a comfort-loving hobbit, is swept into an epic quest to reclaim the Lonely Mountain from the dragon Smaug. Joining a company of thirteen dwarves and the wizard Gandalf, he discovers courage and cunning he never knew he had. Along the way he stumbles upon a mysterious ring that will change the fate of Middle-earth forever.",
            "Fantasy"));
        addBook(new Book("1984", "George Orwell", 1949, 9780451524935L,
            "In a totalitarian future ruled by the omniscient Party, Winston Smith secretly begins to rebel against Big Brother's surveillance state. A searing portrait of propaganda, doublethink, and the systematic destruction of truth and individuality. Orwell's masterwork remains one of the most urgent and chilling novels ever written.",
            "Dystopian"));
        addBook(new Book("To Kill a Mockingbird", "Harper Lee", 1960, 9780061743528L,
            "Seen through the eyes of young Scout Finch in 1930s Alabama, this Pulitzer Prize-winning novel follows her father Atticus as he defends a Black man falsely accused of a crime. A profound and moving exploration of racial injustice, moral courage, and the loss of innocence. One of the defining works of American literature.",
            "Literary Fiction"));
        addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, 9780743273565L,
            "Narrator Nick Carraway is drawn into the world of his mysterious neighbour Jay Gatsby, who throws lavish parties in pursuit of the elusive Daisy Buchanan. A glittering yet melancholy critique of the American Dream and the hollow glamour of the Jazz Age. Fitzgerald's prose shimmers with beauty and moral precision.",
            "Classic"));
        addBook(new Book("Brave New World", "Aldous Huxley", 1932, 9780060850524L,
            "In a scientifically perfected World State, citizens are engineered, conditioned, and kept perpetually content. When a 'Savage' raised outside the system enters this utopia, its fragile illusions begin to crack. Huxley's prophetic satire of consumerism, pleasure, and social control feels startlingly modern.",
            "Sci-Fi"));
        addBook(new Book("The Catcher in the Rye", "J.D. Salinger", 1951, 9780316769174L,
            "Sixteen-year-old Holden Caulfield wanders New York City after being expelled from prep school, wrestling with grief, alienation, and the phoniness of adult life. Salinger's raw, intimate voice captures adolescent disillusionment with startling honesty. One of the most debated and beloved novels in American literature.",
            "Literary Fiction"));
        addBook(new Book("Dune", "Frank Herbert", 1965, 9780441013593L,
            "On the desert planet Arrakis — the sole source of the universe's most precious substance — young Paul Atreides is thrust into a brutal web of political betrayal and religious prophecy. An intricate epic spanning ecology, power, and mysticism, Dune reshaped science fiction. Herbert created one of the most fully realized fictional universes ever committed to the page.",
            "Sci-Fi"));
        addBook(new Book("Fahrenheit 451", "Ray Bradbury", 1953, 9781451673319L,
            "In a future America where books are forbidden and firemen burn them, Guy Montag begins to question everything about the society he serves. Bradbury's blazing novella is a fierce defence of literature, curiosity, and the human capacity for wonder. Written in just nine days on a rented typewriter, it endures as a cornerstone of dystopian fiction.",
            "Dystopian"));
        addBook(new Book("The Name of the Wind", "Patrick Rothfuss", 2007, 9780756404741L,
            "Told in Kvothe's own words, this is the tale of a magically gifted young man who rises from poverty to become the most feared and legendary figure of his age. Rothfuss crafts a richly layered world with prose that reads like music. The Kingkiller Chronicle's opening volume is a modern fantasy landmark.",
            "Fantasy"));
        addBook(new Book("Clean Code", "Robert C. Martin", 2008, 9780132350884L,
            "Robert C. Martin presents a practical philosophy for writing code that is not merely functional, but readable, elegant, and maintainable. Through hundreds of examples, 'Uncle Bob' illuminates the difference between code that works and code that endures. Essential reading for any software developer serious about their craft.",
            "Programming"));
        addBook(new Book("The Pragmatic Programmer", "David Thomas", 1999, 9780135957059L,
            "Hunt and Thomas distil decades of programming experience into timeless wisdom for software craftsmen — from personal responsibility and career development to hands-on technical practices. First published in 1999, its insights remain as relevant as ever in the modern engineering landscape. A book that pays dividends every time you return to it.",
            "Programming"));
        addBook(new Book("Sapiens", "Yuval Noah Harari", 2011, 9780062316097L,
            "From the Cognitive Revolution 70,000 years ago to the present day, Harari charts the sweeping history of our species with intellectual daring and sharp wit. He asks why Homo sapiens came to dominate the Earth and what that dominance costs us and other species. A provocative, wide-ranging journey through human history.",
            "Non-Fiction"));
        addBook(new Book("Thinking, Fast and Slow", "Daniel Kahneman", 2011, 9780374533557L,
            "Nobel laureate Daniel Kahneman reveals the two systems that drive the way we think: the fast, intuitive System 1 and the slow, deliberate System 2. Drawing on decades of groundbreaking research, he exposes the hidden biases that shape our judgements and decisions. A transformative book that permanently changes how you see your own mind.",
            "Non-Fiction"));
        addBook(new Book("The Alchemist", "Paulo Coelho", 1988, 9780062315007L,
            "Santiago, an Andalusian shepherd boy, sets out on a journey to find a treasure rumoured to lie near the Egyptian pyramids. Along the way he encounters a king, an alchemist, and the language of the universe itself. Coelho's beloved fable about following your dreams has sold over 150 million copies worldwide.",
            "Fiction"));
        addBook(new Book("Crime and Punishment", "Fyodor Dostoevsky", 1866, 9780143107637L,
            "Rodion Raskolnikov, a destitute student, commits a murder convinced he is above ordinary morality — then slowly unravels under the suffocating weight of his guilt. Dostoevsky's psychological masterpiece plunges into the depths of sin, suffering, and the possibility of redemption. Widely regarded as one of the greatest novels ever written.",
            "Classic"));
    }

    // ── Data helpers ─────────────────────────────────────────────────

    private void addBook(Book book) {
        books.add(book);
        model.addRow(new Object[]{
            book.getTitle(), book.getAuthor(), book.getYearofPublication(),
            book.getGenre(), book.isBorrowed() ? "Borrowed" : "Available"
        });
        updateCount();
    }

    private void removeSelected() {
        int view = table.getSelectedRow();
        if (view < 0) return;
        int mi = table.convertRowIndexToModel(view);
        books.remove(mi);
        model.removeRow(mi);
        borrowBtn.setEnabled(false);
        updateCount();
        saveData();
    }

    private void applyFilter(String q) {
        String s = q.trim();
        sorter.setRowFilter(s.isEmpty() ? null
            : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(s), 0, 1, 3));
        updateCount();
    }

    private void updateCount() {
        int vis = sorter.getViewRowCount(), tot = books.size();
        long borrowed = books.stream().filter(Book::isBorrowed).count();
        String base = vis == tot ? tot + " books" : vis + " of " + tot;
        countLbl.setText(borrowed > 0 ? base + "  ·  " + borrowed + " borrowed" : base);
    }

    private void clearForm() {
        fTitle.setText(""); fAuthor.setText(""); fYear.setText("");
        fIsbn.setText("");  fGenre.setText("");
    }

    // ── Component builders ────────────────────────────────────────────

    private static JTextField inputField(String hint) {
        return new JTextField() {
            boolean focused;
            {
                setFont(F_BODY); setForeground(TEXT); setOpaque(false);
                setCaretColor(ACCENT);
                setBorder(new EmptyBorder(9, 12, 9, 12));
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
                    @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(MUTED); g3.setFont(F_SMALL);
                    Insets ins = getInsets();
                    g3.drawString(hint, ins.left, getHeight() / 2 + 4);
                    g3.dispose();
                }
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(focused ? ACCENT : SEP);
                g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
    }

    private static JTextField makeSearchField() {
        return new JTextField(16) {
            boolean focused;
            {
                setFont(F_BODY); setForeground(TEXT); setOpaque(false);
                setCaretColor(ACCENT);
                setPreferredSize(new Dimension(210, 34));
                setBorder(new EmptyBorder(6, 14, 6, 14));
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
                    @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(MUTED); g3.setFont(F_SMALL);
                    Insets ins = getInsets();
                    g3.drawString("Search", ins.left, getHeight() / 2 + 4);
                    g3.dispose();
                }
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(focused ? ACCENT : SEP);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
    }

    private static JButton solidBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENTH : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_BODY); b.setForeground(Color.WHITE);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(9, 22, 9, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton ghostBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY); b.setForeground(MUTED);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(DANGER); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(MUTED); }
        });
        return b;
    }

    private static JButton secondaryBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() && isEnabled() ? CARD : BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                setForeground(isEnabled() ? TEXT : MUTED);
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? SEP : new Color(0x3A3A3C));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        b.setFont(F_BODY); b.setForeground(TEXT);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton linkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY); b.setForeground(MUTED);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(TEXT); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(MUTED); }
        });
        return b;
    }

    private static JButton makeXButton() {
        JButton b = new JButton("×");
        b.setFont(sysFont(20, Font.PLAIN));
        b.setForeground(MUTED);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(TEXT); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(MUTED); }
        });
        return b;
    }

    private static Font sysFont(int size, int style) {
        String[] preferred = {"SF Pro Display", "Inter", "Helvetica Neue", "Segoe UI", "Ubuntu", "Cantarell"};
        Set<String> available = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : preferred) {
            if (available.contains(name)) return new Font(name, style, size);
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    private static void shake(Component c) {
        Point orig = c.getLocation();
        javax.swing.Timer t = new javax.swing.Timer(28, null);
        int[] seq = {-7, 7, -5, 5, -3, 3, 0};
        int[] i = {0};
        t.addActionListener(e -> {
            if (i[0] >= seq.length) { t.stop(); c.setLocation(orig); return; }
            c.setLocation(orig.x + seq[i[0]++], orig.y);
        });
        t.start();
        c.requestFocus();
    }
}
