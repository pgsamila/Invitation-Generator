import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class InvitationGenerator {
    private String sourceFileLocation = "Select Image File";
    private String targetFolderLocation = "Select Output Location";
    private String font = "Arial";
    private int fontStyle = Font.PLAIN;
    private int fontSize = 12;
    private String nameListFile = "Select Name List File";
    private ArrayList<String> nameList;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int textPositionX = 0;
    private int textPositionY = 0;
    private boolean isSizesInitialized = false;
    private Color fontColor = Color.RED;
    private int scale = 4;

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getSourceFileLocation() {
        return sourceFileLocation;
    }

    public void setSourceFileLocation(String sourceFileLocation) {
        ImagePlus image = IJ.openImage(sourceFileLocation);
        imageWidth = image.getProcessor().getWidth();
        imageHeight = image.getProcessor().getHeight();
        textPositionX = imageWidth / 2;
        textPositionY = imageHeight / 2;
        isSizesInitialized = true;
        this.sourceFileLocation = sourceFileLocation;
    }

    public String getTargetFolderLocation() {
        return targetFolderLocation;
    }

    public void setTargetFolderLocation(String targetFolderLocation) {
        this.targetFolderLocation = targetFolderLocation;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getFont() {
        return font;
    }

    public void setFontStyle(int style) {
        this.fontStyle = style;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void setNameListFile(String nameListFile) {
        this.nameListFile = nameListFile;
    }

    public String getNameListFile() {
        return nameListFile;
    }

    public int processNames() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(this.nameListFile));
        nameList = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            nameList.add(line);
            line = reader.readLine();
        }
        reader.close();
        return nameList.size();
    }

    public void createImages(JProgressBar progressBar) throws IOException {
        if (targetFolderLocation.equals("Select Output Location")) {
            throw new IOException("Please select output folder and try again");
        }
        int count = 10;
        for (String userName : nameList) {
            String imageName = sourceFileLocation;
            ImagePlus image;
            image = IJ.openImage(imageName);
            if (image == null) {
                throw new IOException("Please select proper source image file and try again");
            }
            ImageProcessor ip = getProcessedImage(image, userName);
            BufferedImage bufferedImage = ip.getBufferedImage();
            File f1 = new File(targetFolderLocation);
            if (!f1.isDirectory()) {
                f1.mkdir();
            }
            File outputFile = new File(targetFolderLocation + "/" + userName + ".jpeg");
            ImageIO.write(bufferedImage, "jpeg", outputFile);
            count = count + 1;
            progressBar.setValue(count);
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void loadPreview(JLabel imagePrevLabel) {
        String imageName = sourceFileLocation;
        ImagePlus image = IJ.openImage(imageName);
        if (image == null) {
            image = IJ.openImage(getClass().getClassLoader().getResource("test.png").getPath());
            if(image == null) {
                image = IJ.openImage("./test.png");
            }
            if(image == null){
                return;
            }
            if (!isSizesInitialized) {
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
                textPositionX = imageWidth / 2;
                textPositionY = imageHeight / 2;
                isSizesInitialized = true;
            }
        }
        String text = "Test Word ^ droW tseT";
        ImageProcessor ip = getProcessedImage(image, text);
        imagePrevLabel.setIcon(new ImageIcon(ip.getBufferedImage().getScaledInstance(imageWidth / scale, imageHeight / scale, 0)));
        imagePrevLabel.setText("");
    }

    private ImageProcessor getProcessedImage(ImagePlus image, String text) {
        Font font = new Font(getFont(), getFontStyle(), getFontSize());
        ImageProcessor ip = image.getProcessor();
        ip.setColor(fontColor);
        ip.setFont(font);
        ip.setJustification(ImageProcessor.CENTER_JUSTIFY);
        ip.drawString(text, textPositionX, textPositionY);
        image.flush();
        ip.snapshot();
        return ip;
    }

    public void setXPosition(int value) {
        this.textPositionX = value;
    }

    public int getTextPositionX() {
        return textPositionX;
    }

    public void setYPosition(int value) {
        this.textPositionY = value;
    }

    public int getTextPositionY() {
        return textPositionY;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }
}
