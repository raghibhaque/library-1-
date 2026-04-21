package monty.library;

import monty.library.kyle.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryGUI extends JFrame {

    // ── Palette — macOS dark mode ────────────────────────────────
    private static final Color BG      = new Color(0x111113);
    private static final Color SURFACE = new Color(0x1C1C1E);
    private static final Color CARD    = new Color(0x2C2C2E);
    private static final Color SEP     = new Color(0x38383A);
    private static final Color TEXT    = new Color(0xF5F5F7);
    private static final Color MUTED   = new Color(0x86868B);
    private static final Color ACCENT  = new Color(0x0A84FF);
    private static final Color ACCENTH = new Color(0x409CFF);
    private static final Color DANGER  = new Color(0xFF453A);
    private static final Color SEL_BG  = new Color(0x09213A);
    private static final Color SEL_FG  = new Color(0x60AEFF);
    private static final Color ROW_SEP = new Color(0x26262A);

    private static final Font F_TITLE = sysFont(24, Font.BOLD);
    private static final Font F_BODY  = sysFont(13, Font.PLAIN);
    private static final Font F_SMALL = sysFont(11, Font.PLAIN);
    private static final Font F_HEAD  = sysFont(10, Font.BOLD);

    private final List<Book> books = new ArrayList<>();
    private final DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private final JLabel countLbl = new JLabel();

    private final JTextField fTitle  = inputField("Title");
    private final JTextField fAuthor = inputField("Author");
    private final JTextField fYear   = inputField("Year");
    private final JTextField fIsbn   = inputField("ISBN");

    public LibraryGUI() {
        setTitle("Library");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(root);

        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));

        JLabel titleLbl = new JLabel("Library");
        titleLbl.setFont(F_TITLE);
        titleLbl.setForeground(TEXT);

        JTextField searchField = searchField();

        countLbl.setFont(F_SMALL);
        countLbl.setForeground(MUTED);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);
        headerRight.add(searchField);
        headerRight.add(countLbl);

        header.add(titleLbl,    BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────
        String[] cols = {"Title", "Author", "Year", "ISBN"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = buildTable();
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        root.add(buildScroll(table), BorderLayout.CENTER);

        // ── Bottom form ──────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel divider = new JPanel();
        divider.setBackground(SEP);
        divider.setPreferredSize(new Dimension(0, 1));
        bottom.add(divider, BorderLayout.NORTH);

        JPanel inputs = new JPanel(new GridLayout(1, 4, 10, 0));
        inputs.setOpaque(false);
        inputs.setBorder(new EmptyBorder(14, 0, 0, 0));
        inputs.add(fTitle); inputs.add(fAuthor);
        inputs.add(fYear);  inputs.add(fIsbn);
        bottom.add(inputs, BorderLayout.CENTER);

        JButton removeBtn = ghostBtn("Remove");
        JButton addBtn    = solidBtn("Add Book");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(removeBtn);
        btns.add(addBtn);
        bottom.add(btns, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        // ── Seed & wire ──────────────────────────────────────────
        seed();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(searchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(searchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
        });

        // Double-click → detail window
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int view = table.getSelectedRow();
                if (view < 0) return;
                showDetail(books.get(table.convertRowIndexToModel(view)));
            }
        });

        addBtn.addActionListener(e -> {
            try {
                String t = fTitle.getText().trim(), a = fAuthor.getText().trim();
                if (t.isEmpty() || a.isEmpty()) { shake(t.isEmpty() ? fTitle : fAuthor); return; }
                int yr      = Integer.parseInt(fYear.getText().trim());
                double isbn = Double.parseDouble(fIsbn.getText().trim());
                addBook(new Book(t, a, yr, isbn));
                clearForm();
            } catch (NumberFormatException ex) {
                shake(fYear.getText().trim().isEmpty() ? fYear : fIsbn);
            }
        });

        removeBtn.addActionListener(e -> {
            int view = table.getSelectedRow();
            if (view < 0) return;
            int mi = table.convertRowIndexToModel(view);
            books.remove(mi);
            model.removeRow(mi);
            updateCount();
        });

        setSize(860, 600);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
    }

    // ── Detail window ────────────────────────────────────────────

    private void showDetail(Book book) {
        JDialog d = new JDialog(this, false);
        d.setUndecorated(true);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(SURFACE);
        main.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SEP),
            new EmptyBorder(24, 28, 20, 28)
        ));

        // ── Top info ─────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // × button
        JButton xBtn = new JButton("×");
        xBtn.setFont(sysFont(20, Font.PLAIN));
        xBtn.setForeground(MUTED);
        xBtn.setOpaque(false); xBtn.setContentAreaFilled(false);
        xBtn.setFocusPainted(false); xBtn.setBorderPainted(false);
        xBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        xBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        xBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { xBtn.setForeground(TEXT); }
            @Override public void mouseExited(MouseEvent e)  { xBtn.setForeground(MUTED); }
        });
        xBtn.addActionListener(e -> d.dispose());

        JPanel xRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        xRow.setOpaque(false);
        xRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        xRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        xRow.add(xBtn);

        JSeparator divider = new JSeparator();
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setForeground(SEP);

        info.add(xRow);
        info.add(Box.createVerticalStrut(8));
        info.add(infoLabel(book.getTitle(), sysFont(20, Font.BOLD), TEXT));
        info.add(Box.createVerticalStrut(6));
        info.add(infoLabel(book.getAuthor() + "  ·  " + book.getYearofPublication(), F_BODY, MUTED));
        info.add(Box.createVerticalStrut(3));
        info.add(infoLabel("ISBN  " + (long) book.getIsbn(), F_SMALL, new Color(0x505054)));
        info.add(Box.createVerticalStrut(18));
        info.add(divider);

        // ── Description ──────────────────────────────────────────
        String descText = book.getDescription().isEmpty()
            ? "No description available." : book.getDescription();

        JTextArea desc = new JTextArea(descText);
        desc.setFont(F_BODY);
        desc.setForeground(new Color(0xBEBEC6));
        desc.setBackground(SURFACE);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFocusable(false);
        desc.setBorder(new EmptyBorder(16, 0, 0, 0));

        JScrollPane descScroll = new JScrollPane(desc,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descScroll.setBorder(null);
        descScroll.setBackground(SURFACE);
        descScroll.getViewport().setBackground(SURFACE);
        descScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x48484A); trackColor = SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return tinyBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return tinyBtn(); }
            private JButton tinyBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });

        // ── Close button ─────────────────────────────────────────
        JButton closeBtn = solidBtn("Close");
        closeBtn.addActionListener(e -> d.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        btnRow.add(closeBtn);

        // Escape to close
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        main.add(info,       BorderLayout.NORTH);
        main.add(descScroll, BorderLayout.CENTER);
        main.add(btnRow,     BorderLayout.SOUTH);

        d.setContentPane(main);
        d.setSize(480, 340);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private static JLabel infoLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Table ────────────────────────────────────────────────────

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
        t.setFocusable(false);

        t.getColumnModel().getColumn(0).setPreferredWidth(240);
        t.getColumnModel().getColumn(1).setPreferredWidth(175);
        t.getColumnModel().getColumn(2).setPreferredWidth(55);
        t.getColumnModel().getColumn(3).setPreferredWidth(150);

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

    private JScrollPane buildScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(SEP));
        sp.getViewport().setBackground(SURFACE);
        sp.setBackground(SURFACE);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x48484A); trackColor = SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return tinyBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return tinyBtn(); }
            private JButton tinyBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
        return sp;
    }

    // ── Seed ─────────────────────────────────────────────────────

    private void seed() {
        addBook(new Book("The Hobbit", "J.R.R. Tolkien", 1937, 9780547928227.0,
            "Bilbo Baggins, a comfort-loving hobbit, is swept into an epic quest to reclaim the Lonely Mountain from the dragon Smaug. Joining a company of thirteen dwarves and the wizard Gandalf, he discovers courage and cunning he never knew he had. Along the way he stumbles upon a mysterious ring that will change the fate of Middle-earth forever."));
        addBook(new Book("1984", "George Orwell", 1949, 9780451524935.0,
            "In a totalitarian future ruled by the omniscient Party, Winston Smith secretly begins to rebel against Big Brother's surveillance state. A searing portrait of propaganda, doublethink, and the systematic destruction of truth and individuality. Orwell's masterwork remains one of the most urgent and chilling novels ever written."));
        addBook(new Book("To Kill a Mockingbird", "Harper Lee", 1960, 9780061743528.0,
            "Seen through the eyes of young Scout Finch in 1930s Alabama, this Pulitzer Prize-winning novel follows her father Atticus as he defends a Black man falsely accused of a crime. A profound and moving exploration of racial injustice, moral courage, and the loss of innocence. One of the defining works of American literature."));
        addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, 9780743273565.0,
            "Narrator Nick Carraway is drawn into the world of his mysterious neighbour Jay Gatsby, who throws lavish parties in pursuit of the elusive Daisy Buchanan. A glittering yet melancholy critique of the American Dream and the hollow glamour of the Jazz Age. Fitzgerald's prose shimmers with beauty and moral precision."));
        addBook(new Book("Brave New World", "Aldous Huxley", 1932, 9780060850524.0,
            "In a scientifically perfected World State, citizens are engineered, conditioned, and kept perpetually content. When a 'Savage' raised outside the system enters this utopia, its fragile illusions begin to crack. Huxley's prophetic satire of consumerism, pleasure, and social control feels startlingly modern."));
        addBook(new Book("The Catcher in the Rye", "J.D. Salinger", 1951, 9780316769174.0,
            "Sixteen-year-old Holden Caulfield wanders New York City after being expelled from prep school, wrestling with grief, alienation, and the phoniness of adult life. Salinger's raw, intimate voice captures adolescent disillusionment with startling honesty. One of the most debated and beloved novels in American literature."));
        addBook(new Book("Dune", "Frank Herbert", 1965, 9780441013593.0,
            "On the desert planet Arrakis — the sole source of the universe's most precious substance — young Paul Atreides is thrust into a brutal web of political betrayal and religious prophecy. An intricate epic spanning ecology, power, and mysticism, Dune reshaped science fiction. Herbert created one of the most fully realized fictional universes ever committed to the page."));
        addBook(new Book("Fahrenheit 451", "Ray Bradbury", 1953, 9781451673319.0,
            "In a future America where books are forbidden and firemen burn them, Guy Montag begins to question everything about the society he serves. Bradbury's blazing novella is a fierce defence of literature, curiosity, and the human capacity for wonder. Written in just nine days on a rented typewriter, it endures as a cornerstone of dystopian fiction."));
        addBook(new Book("The Name of the Wind", "Patrick Rothfuss", 2007, 9780756404741.0,
            "Told in Kvothe's own words, this is the tale of a magically gifted young man who rises from poverty to become the most feared and legendary figure of his age. Rothfuss crafts a richly layered world with prose that reads like music. The Kingkiller Chronicle's opening volume is a modern fantasy landmark."));
        addBook(new Book("Clean Code", "Robert C. Martin", 2008, 9780132350884.0,
            "Robert C. Martin presents a practical philosophy for writing code that is not merely functional, but readable, elegant, and maintainable. Through hundreds of examples, 'Uncle Bob' illuminates the difference between code that works and code that endures. Essential reading for any software developer serious about their craft."));
        addBook(new Book("The Pragmatic Programmer", "David Thomas", 1999, 9780135957059.0,
            "Hunt and Thomas distil decades of programming experience into timeless wisdom for software craftsmen — from personal responsibility and career development to hands-on technical practices. First published in 1999, its insights remain as relevant as ever in the modern engineering landscape. A book that pays dividends every time you return to it."));
        addBook(new Book("Sapiens", "Yuval Noah Harari", 2011, 9780062316097.0,
            "From the Cognitive Revolution 70,000 years ago to the present day, Harari charts the sweeping history of our species with intellectual daring and sharp wit. He asks why Homo sapiens came to dominate the Earth and what that dominance costs us and other species. A provocative, wide-ranging journey through human history."));
        addBook(new Book("Thinking, Fast and Slow", "Daniel Kahneman", 2011, 9780374533557.0,
            "Nobel laureate Daniel Kahneman reveals the two systems that drive the way we think: the fast, intuitive System 1 and the slow, deliberate System 2. Drawing on decades of groundbreaking research, he exposes the hidden biases that shape our judgements and decisions. A transformative book that permanently changes how you see your own mind."));
        addBook(new Book("The Alchemist", "Paulo Coelho", 1988, 9780062315007.0,
            "Santiago, an Andalusian shepherd boy, sets out on a journey to find a treasure rumoured to lie near the Egyptian pyramids. Along the way he encounters a king, an alchemist, and the language of the universe itself. Coelho's beloved fable about following your dreams has sold over 150 million copies worldwide."));
        addBook(new Book("Crime and Punishment", "Fyodor Dostoevsky", 1866, 9780143107637.0,
            "Rodion Raskolnikov, a destitute student, commits a murder convinced he is above ordinary morality — then slowly unravels under the suffocating weight of his guilt. Dostoevsky's psychological masterpiece plunges into the depths of sin, suffering, and the possibility of redemption. Widely regarded as one of the greatest novels ever written."));
    }

    // ── Data helpers ─────────────────────────────────────────────

    private void addBook(Book book) {
        books.add(book);
        model.addRow(new Object[]{book.getTitle(), book.getAuthor(), book.getYearofPublication(), (long) book.getIsbn()});
        updateCount();
    }

    private void applyFilter(String q) {
        String s = q.trim();
        sorter.setRowFilter(s.isEmpty() ? null
            : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(s), 0, 1));
        updateCount();
    }

    private void updateCount() {
        int vis = sorter.getViewRowCount(), tot = books.size();
        countLbl.setText(vis == tot ? tot + " books" : vis + " of " + tot);
    }

    private void clearForm() {
        fTitle.setText(""); fAuthor.setText(""); fYear.setText(""); fIsbn.setText("");
    }

    // ── Component builders ───────────────────────────────────────

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

    private static JTextField searchField() {
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
