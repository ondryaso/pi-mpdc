package eu.ondryaso.mpcdisplay;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiFactory;
import eu.ondryaso.ssd1306.Display;
import org.bff.javampd.player.Player;
import org.bff.javampd.server.MPD;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main {
    private MPD mpd;
    private Display display;
    private BufferedImage play, next;

    public Main() throws IOException, InterruptedException {
        GpioController c = GpioFactory.getInstance();

        Display d = new Display(128, 64, c,
                SpiFactory.getInstance(SpiChannel.CS1), RaspiPin.GPIO_04);

        d.begin();
        d.display();
        d.getGraphics().setFont(new Font("Monospaced", Font.PLAIN, 10));
        this.display = d;
        this.loadImages();

        MPD.Builder b = new MPD.Builder();
        b.server("10.0.0.25");
        this.mpd = b.build();

        mpd.getMonitor().addPlayerChangeListener(e -> this.drawControls());
        mpd.getMonitor().addPlaylistChangeListener(e -> this.drawInfo());
        mpd.getMonitor().addTrackPositionChangeListener(e -> this.drawPosition());
        mpd.getMonitor().start();

        GpioPinDigitalInput playBtn = c.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
        GpioPinDigitalInput nextBtn = c.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_UP);

        c.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                if (mpd.getPlayer().getStatus() == Player.Status.STATUS_PLAYING) {
                    mpd.getPlayer().pause();
                } else {
                    mpd.getPlayer().play();
                }
                this.drawControls();
            }
        }, playBtn);

        c.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                mpd.getPlayer().playNext();
                this.drawInfo();
            }
        }, nextBtn);

        while (true) {
            Thread.sleep(10000);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Main m = new Main();
    }

    public void loadImages() throws IOException {
        this.play = ImageIO.read(this.getClass().getResourceAsStream("play.png"));
        this.next = ImageIO.read(this.getClass().getResourceAsStream("next.png"));
    }

    public void drawInfo() {
        this.display.getGraphics().clearRect(0, 0, this.display.getWidth(), 40);

        Player p = this.mpd.getPlayer();

        if(p.getStatus() != Player.Status.STATUS_STOPPED) {
            this.drawStringCentered(p.getCurrentSong().getTitle(), 10);
            this.drawStringCentered(p.getCurrentSong().getAlbumName(), 20);
        } else {
            this.drawStringCentered("Stopped", 10);
        }

        this.display.displayImage();
    }

    public void drawControls() {
        Graphics2D g = this.display.getGraphics();
        g.clearRect(0, 40, this.display.getWidth(), 20);

        int y = 40;
        int cX = 31;
        int nX = 81;

        switch (this.mpd.getPlayer().getStatus()) {
            case STATUS_STOPPED:
                this.drawPlay(cX, y);
                break;
            case STATUS_PLAYING:
                this.drawPause(cX, y);
                break;
            case STATUS_PAUSED:
                this.drawPlay(cX, y);
                break;
        }

        this.drawNext(nX, y);

        this.display.displayImage();
    }

    public void drawPosition() {
        Graphics2D g = this.display.getGraphics();
        g.clearRect(0, 60, this.display.getWidth(), 4);

        float pos = (float)this.mpd.getPlayer().getElapsedTime() / (float)this.mpd.getPlayer().getTotalTime();
        g.fillRect((int) (this.display.getWidth() * pos), 60, 3, 4);

        this.display.displayImage();
    }


    //region Lower drawing methods
    public void drawPause(int x, int y) {
        Graphics2D g = this.display.getGraphics();
        g.fillRect(x + 1, y, 4, 15);
        g.fillRect(x + 10, y, 4, 15);
    }

    public void drawPlay(int x, int y) {
        this.display.getGraphics().drawImage(this.play, x, y, null);
    }

    public void drawNext(int x, int y) {
        this.display.getGraphics().drawImage(this.next, x + 1, y + 2, null);
    }

    public void drawStringCentered(String s, int y) {
        Graphics2D g = this.display.getGraphics();
        if(s.length() > 18)
            s = s.substring(0, 16).trim() + "...";
        g.drawString(s, this.display.getWidth() / 2 -
                g.getFontMetrics().stringWidth(s) / 2, y);
    }
    //#endregion
}
