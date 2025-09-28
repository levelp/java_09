import javax.swing.*;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * Основная форма приложения
 */
public class MainForm {
    DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> primesList;
    private JPanel panel;

    MainForm() {
        primesList.setModel(model);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Список простых чисел");
        MainForm form = new MainForm();
        frame.setContentPane(form.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                invokeLater(() -> form.model.addElement("2"));
                for (int i = 2; i < Integer.MAX_VALUE; i++) {
                    if (i % 1000 == 0) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isPrime(i)) {
                        final int j = i;
                        invokeLater(() -> form.model.addElement("" + j));
                    }
                }
            }

            private boolean isPrime(int N) {
                int h = (int) Math.ceil(Math.sqrt(N));
                for (int i = 2; i <= h; i++) {
                    if (N % i == 0)
                        return false;
                }
                return true;
            }
        }).start();
    }
}
