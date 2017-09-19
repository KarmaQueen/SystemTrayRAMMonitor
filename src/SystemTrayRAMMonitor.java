import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;

/**
 * A Tool that Shows your CPU/RAM usage on your SystemTray (icons on the bottom-right of your screen, on the taskbar)
 * Created by 44067301 on 9/1/2017.
 */
public class SystemTrayRAMMonitor {

    private static final String
            strGetSystemCpuLoad = "getSystemCpuLoad",
            strGetTotalPhysicalMemorySize = "getTotalPhysicalMemorySize",
            strGetFreePhysicalMemorySize = "getFreePhysicalMemorySize"
            ;//strGetOccupiedPhysicalMemorySize = "getOccupiedPhysicalMemorySize";

    private static long
            getTotalPhysicalMemorySize,
            getFreePhysicalMemorySize,
            getOccupiedPhysicalMemorySize;

    private static String
            strOccupiedRAM = "";

    private static final PopupMenu popup = new PopupMenu();
    private static Image image = Toolkit.getDefaultToolkit().createImage("default");
    private static TrayIcon trayIcon = new TrayIcon(image, "RAM Monitor");
    private static SystemTray tray = SystemTray.getSystemTray();

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df = new DecimalFormat("#.#");

    private static boolean done = false;
    private static long delay = 1000;

    public static void main(final String[] args){
        if(!SystemTray.isSupported()){
            System.out.println("SystemTray is not supported!");
            return;
        }

        //Start Usage getting services
        initUsage();
        getUsage();

        //Init Tray
        MenuItem delay1min = new MenuItem("1 minute");
        MenuItem delay30sec = new MenuItem("30 Seconds");
        MenuItem delay10sec = new MenuItem("10 Seconds");
        MenuItem delay5sec = new MenuItem("5 seconds");
        MenuItem delay1sec = new MenuItem("1 second");
        MenuItem delay500ms = new MenuItem("500 ms");
        MenuItem delay200ms =  new MenuItem("200 ms");
        MenuItem delay100ms = new MenuItem("100 ms");
        MenuItem exitItem = new MenuItem("Exit");

        delay1min.addActionListener(    e -> delay = 1000*60);
        delay30sec.addActionListener(   e -> delay = 1000*30);
        delay10sec.addActionListener(   e -> delay = 1000*10);
        delay5sec.addActionListener(    e -> delay = 1000*5);
        delay1sec.addActionListener(    e -> delay = 1000*1);
        delay500ms.addActionListener(   e -> delay = 500);
        delay200ms.addActionListener(   e -> delay = 200);
        delay100ms.addActionListener(   e -> delay = 100);
        exitItem.addActionListener(     e -> System.exit(0));

        popup.add(delay1min);
        popup.add(delay30sec);
        popup.add(delay10sec);
        popup.add(delay5sec);
        popup.add(delay1sec);
        popup.add(delay500ms);
        popup.add(delay200ms);
        popup.add(delay100ms);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try{
            tray.add(trayIcon);
        } catch(AWTException e){
            System.out.println("TrayIcon could not be added.");
        }

        while(!done){
            getUsage();
            convertLongToGigabytes();
            setTextAsIcon(strOccupiedRAM);
            try {
                Thread.sleep(delay);
            } catch(Exception e){
                e.printStackTrace();
                done = true;
            }
        }

    }

    private static void convertLongToGigabytes(){
        double ram = (double)getOccupiedPhysicalMemorySize/1024/1024/1024;
        strOccupiedRAM = df.format(ram);
        trayIcon.setToolTip("RAM Usage: " + df2.format(ram));
    }

    private static void setTextAsIcon(String str){
        image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = ((BufferedImage)image).createGraphics();
        g2d.setFont(new Font("Haettenschweiler", Font.PLAIN, 12));
        g2d.drawString(str, 0, 12);
        g2d.dispose();
        trayIcon.setImage(image);
    }

    /**
     * Just gets your total memory.
     */
    private static void initUsage(){
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        for(Method method : operatingSystemMXBean.getClass().getDeclaredMethods()){
            method.setAccessible(true);

            if(strGetTotalPhysicalMemorySize.equals(method.getName()) && Modifier.isPublic(method.getModifiers())){
                Object val;
                try {
                    val = method.invoke(operatingSystemMXBean);
                } catch(Exception e){
                    val = e;
                }
                getTotalPhysicalMemorySize = Long.parseLong(val.toString());
            }
        }
    }

    /**
     * Updates CPU and RAM usage to their respective static long fields.
     */
    private static void getUsage(){
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for(Method method : operatingSystemMXBean.getClass().getDeclaredMethods()){
            method.setAccessible(true);

            if(method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())){
                Object val;
                try {
                    val = method.invoke(operatingSystemMXBean);
                } catch(Exception e){
                    val = e;
                }
                System.out.println(method.getName() + " = " + val);

                switch(method.getName()){
                    case strGetSystemCpuLoad:
                        //getSystemCpuLoad = Double.parseDouble(val.toString());
                        break;
                    case strGetFreePhysicalMemorySize:
                        getFreePhysicalMemorySize = Long.parseLong(val.toString());
                        break;
                    default:
                }
            }
        }
        getOccupiedPhysicalMemorySize = getTotalPhysicalMemorySize - getFreePhysicalMemorySize;
    }
}
