package SwingGUI;

import JSON_Simple.src.main.java.org.json.simple.JSONObject;
import JSON_Simple.src.main.java.org.json.simple.parser.JSONParser;
import JSON_Simple.src.main.java.org.json.simple.parser.ParseException;
import ScriptClasses.Util.Statics;
import org.osbot.rs07.script.Script;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class GUI {
    private Script script;
    private GUIResults results;
    private JFrame frame;
    private JPanel panel;
    private JSpinner overloadCountSpinner;
    private JSpinner absorptionCountSpinner;
    private JSlider activeSlider;
    private JSlider passiveSlider;
    private JComboBox specialWeaponDropDown;
    private JLabel overloadLabel;
    private JLabel absorptionLabel;
    private JLabel activeLabel;
    private JLabel passiveLabel;
    private JLabel specWeaponLabel;
    private JLabel activePercentLabel;
    private JLabel passivePercentLabel;
    private JCheckBox firstDreamBox;
    private JLabel firstDreamLabel;
    private JTextArea firstDreamText;
    private JButton confirmButton;
    private JTextArea potionLimitText;
    private JButton loadButton;
    private boolean guiActive;

    private final static String SETTINGS_FILE = "Imperishable_NMZ/settings.txt";

    public static void main(String[] args) {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI(null).panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public GUI(Script script){
        this.script = script;
        setUp();
    }

    private void setUp(){
        guiActive = true;
        frame = new JFrame("Imperishable_NMZ");
        frame.setContentPane(this.panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUpSliders();
        setUpButtons();
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents(){
        overloadCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 27, 1));
        absorptionCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 27, 1));

        overloadCountSpinner.addChangeListener(e -> {
            overloadCountSpinner = (JSpinner) e.getSource();
            int overloadValue = (int) overloadCountSpinner.getValue();
            int absorptionValue = (int) absorptionCountSpinner.getValue();
            if(overloadValue + absorptionValue > 27){
                potionLimitText.setForeground(Color.RED);
            }
            else{
                potionLimitText.setForeground(Color.GREEN);
            }
        });
    }

    private void setUpSliders(){
        activeSlider.setSnapToTicks(true);
        activeSlider.setMajorTickSpacing(10);
        activeSlider.setPaintTicks(true);
        activeSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            int activePercent = slider.getValue();
            int passivePercent = 100 - activePercent;
            activePercentLabel.setText(activePercent + "%");
            passivePercentLabel.setText(passivePercent + "%");
            passiveSlider.setValue(passivePercent);
        });

        passiveSlider.setSnapToTicks(true);
        passiveSlider.setMajorTickSpacing(10);
        passiveSlider.setPaintTicks(true);
        passiveSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            int passivePercent = slider.getValue();
            int activePercent = 100 - passivePercent;
            passivePercentLabel.setText(passivePercent + "%");
            activePercentLabel.setText(activePercent + "%");
            activeSlider.setValue(activePercent);
        });
    }

    private void setUpButtons(){
        confirmButton.addActionListener(e -> {
            int overloadValue = (int) overloadCountSpinner.getValue();
            int absorptionValue = (int) absorptionCountSpinner.getValue();
            if(overloadValue + absorptionValue <= 27){
                results = new GUIResults(firstDreamBox.isSelected(), (int) overloadCountSpinner.getValue(),
                        (int) absorptionCountSpinner.getValue(), activeSlider.getValue());
                String jsonifiedResults = results.toJSON();
                String fileLocation;

                if(Statics.staticScriptRef == null)
                    fileLocation = "C:/Users/Yifan/OSBot/Data/"; //for running GUI's main()
                else
                    fileLocation = Statics.staticScriptRef.getDirectoryData();

                File f = new File(fileLocation + SETTINGS_FILE);
                FileWriter writer = null;
                try {
                    writer = new FileWriter(f);
                    writer.write(jsonifiedResults);
                } catch (IOException | NullPointerException e1) {
                    e1.printStackTrace(); //likely to do with script being null because main in this class was ran
                } finally {
                    if(writer != null){
                        try {
                            writer.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                guiActive = false;
                frame.dispose();
            }
        });

        loadButton.addActionListener(e -> {
            try{
                String fileLocation;
                if(Statics.staticScriptRef == null)
                    fileLocation = "C:/Users/Yifan/OSBot/Data/";
                else
                    fileLocation = Statics.staticScriptRef.getDirectoryData();

                JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(fileLocation + SETTINGS_FILE));
                boolean prayerDream = (boolean) jsonObject.get("prayerDream");
                int numOverloads = ((Long) jsonObject.get("numOverloads")).intValue();
                int numAbsorptions = ((Long) jsonObject.get("numAbsorptions")).intValue();
                int activeUsagePercent = ((Long) jsonObject.get("activeUsagePercent")).intValue();
                this.firstDreamBox.setSelected(prayerDream);
                this.passiveSlider.setValue(1 - activeUsagePercent);
                this.activeSlider.setValue(activeUsagePercent);
                this.absorptionCountSpinner.setValue(numAbsorptions);
                this.overloadCountSpinner.setValue(numOverloads);
            } catch(FileNotFoundException ex){
                this.firstDreamBox.setSelected(false);
                this.passiveSlider.setValue(25);
                this.activeSlider.setValue(75);
                this.absorptionCountSpinner.setValue(17);
                this.overloadCountSpinner.setValue(10);
            } catch (IOException | ParseException ex) {
                ex.printStackTrace();
            }
        });
    }

    public boolean isGuiActive() {
        return guiActive;
    }

    public GUIResults getResults() {
        return results;
    }
}
