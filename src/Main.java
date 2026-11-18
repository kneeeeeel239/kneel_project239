import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main extends JFrame {
    public BufferedImage pers;
    public BufferedImage background;

    // Размеры окна
    private final int WIDTH = 1920;
    private final int HEIGHT = 1080;

    // Координаты и размеры персонажа
    private int x = 100;
    private int y = 100;
    private final int persWidth = 200;
    private final int persHeight = 150;
    private final int speed = 7;

    // Физика прыжка и гравитации
    private double velocityY = 0;
    private final double gravity = 0.7;
    private final double jumpPower = -18;
    private boolean isGrounded = false;
    // Координата Y верхней границы пола
    private final int groundY = 807;

    // ПРЕПЯТСТВИЕ: x = 700, y = 800 (стоит на земле 920 - 120), ширина = 120, высота = 120
    private final Rectangle obstacle = new Rectangle(90, groundY - 150, 450, 150);

    // Флаги нажатых клавиш
    private boolean left, right;

    public Main() {
        try {
            pers = ImageIO.read(new File("src/pictures/south.png"));
            background = ImageIO.read(new File("src/pictures/image0.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setTitle("Платформер с препятствием");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Рисуем фон
                if (background != null) {
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                }

                /* Рисуем пол
                g.setColor(new Color(60, 179, 113, 180));
                g.fillRect(0, groundY, getWidth(), getHeight() - groundY);


                g.setColor(new Color(139, 69, 19));
                g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);*/


                if (pers != null) {
                    g.drawImage(pers, x, y, persWidth, persHeight, this);
                }
            }
        };
        add(panel);

        // Управление с клавиатуры
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) left = true;
                if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = true;

                if ((code == KeyEvent.VK_SPACE || code == KeyEvent.VK_W || code == KeyEvent.VK_UP) && isGrounded) {
                    velocityY = jumpPower;
                    isGrounded = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) left = false;
                if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = false;
            }
        });

        // Игровой цикл (~60 FPS)
        Timer timer = new Timer(16, e -> {
            // 1. ДВИЖЕНИЕ И СТОЛКНОВЕНИЯ ПО ОСИ X
            if (left) x -= speed;
            if (right) x += speed;

            // Ограничение по краям окна
            x = Math.max(0, Math.min(x, panel.getWidth() - persWidth));

            // Создаем хитбокс персонажа после сдвига по X
            Rectangle playerBox = new Rectangle(x, y, persWidth, persHeight);
            if (playerBox.intersects(obstacle)) {
                if (right) {
                    // Уперлись в левый бок препятствия
                    x = obstacle.x - persWidth;
                } else if (left) {
                    // Уперлись в правый бок препятствия
                    x = obstacle.x + obstacle.width;
                }
            }

            // 2. ДВИЖЕНИЕ И СТОЛКНОВЕНИЯ ПО ОСИ Y (ГРАВИТАЦИЯ)
            velocityY += gravity;
            y += (int) velocityY;
            isGrounded = false; // Сбрасываем флаг, пока не подтвердим опору под ногами

            // Обновляем хитбокс после сдвига по Y
            playerBox.setBounds(x, y, persWidth, persHeight);
            if (playerBox.intersects(obstacle)) {
                if (velocityY > 0) {
                    // Приземлились сверху на препятствие
                    y = obstacle.y - persHeight;
                    velocityY = 0;
                    isGrounded = true;
                } else if (velocityY < 0) {
                    // Ударились головой о нижнюю грань препятствия
                    y = obstacle.y + obstacle.height;
                    velocityY = 0;
                }
            }

            // Проверка приземления на основной пол
            int floorLevel = groundY - persHeight;
            if (y >= floorLevel) {
                y = floorLevel;
                velocityY = 0;
                isGrounded = true;
            }

            panel.repaint();
        });
        timer.start();

        setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}
