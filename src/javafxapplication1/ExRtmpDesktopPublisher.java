/**
 * Copyright (c) 2006 - 2009 Smaxe Ltd (www.smaxe.com).
 * All rights reserved.
 */

import com.smaxe.io.ByteArray;
import com.smaxe.uv.client.ICamera;
import com.smaxe.uv.client.IMicrophone;
import com.smaxe.uv.client.camera.AbstractCamera;
import com.smaxe.uv.client.rtmp.INetConnection;
import com.smaxe.uv.client.rtmp.INetStream;
import com.smaxe.uv.stream.MediaDataFactory;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javafx.application.Application;
import javafx.stage.Stage;
import javafxapplication1.DirectTestPlayer;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import net.coobird.thumbnailator.Thumbnails;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * <code>ExRtmpDesktopPublisher</code> - publishes part of Desktop screen to the RTMP server.
 * <p> Note:
 * <br> - This example encodes desktop using ScreenVideo codec implementation.
 * <br> - Voice publisher example is available at <a href="http://www.smaxe.com/source.jsf?id=ExRtmpVoicePublisher.java" target="_blank">Voice publisher (Java class)</a>
 * <br> - ExRtmpDesktopPublisher eXtension that adds upload bandwidth management is available at
 * <a href="http://www.smaxe.com/source.jsf?id=ExRtmpDesktopPublisherX.java" target="_blank">ExRtmpDesktopPublisher eXtenstion (Java class)</a>
 * <br> - Desktop Publisher example based on ScreenVideo2 codec is provided to customers for free during support period.
 * 
 * @author Andrei Sochirca
 * @see <a href="http://www.smaxe.com/product.jsf?id=juv-rtmp-client" target="_blank">JUV RTMP Client</a>
 * @see <a href="http://www.smaxe.com/product.jsf?id=juv-rtmfp-client" target="_blank">JUV RTMFP/RTMP Client</a>
 */
public final class ExRtmpDesktopPublisher extends Application
{
    /**
     * Entry point.
     * 
     * @param args
     * @throws Exception if an exception occurred
     */
private static EmbeddedMediaPlayerComponent mediaPlayerComponent;
    public static void main(final String[] args) throws Exception
    {
        // NOTE:
        // you can get Evaluation Key at:
        // http://www.smaxe.com/order.jsf#request_evaluation_key
        // or buy at:
        // http://www.smaxe.com/order.jsf
        
        // Android-specific:
        // - please add permission to the AndroidManifest.xml : 
        // <uses-permission android:name="android.permission.INTERNET" />
        // - please use separate thread to connect to the server (not UI thread) : 
        // NetConnection#connect() connects to the remote server in the invocation thread,
        // so it causes NetworkOnMainThreadException on 4.0 (http://developer.android.com/reference/android/os/NetworkOnMainThreadException.html)
        
        // YouTube-specific:
        // - YouTube requires both audio and video stream
//        com.smaxe.uv.client.rtmp.License.setKey("52503-02317-77600-8DBC9-8F1D8");
         System.setProperty("jna.library.path", "/Applications/VLC.app/Contents/MacOS/lib");

         DirectTestPlayer player = new DirectTestPlayer("rtmp://34.227.142.101:1935/myapp/test", new String[0]);

//            
//            JFrame frame = new JFrame();
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setLayout(null);
//            frame.setLocation(100, 50);
//            frame.setSize(800, 600);
//        
//        if(mediaPlayerComponent == null) {
//               mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
//               mediaPlayerComponent.setLocation(50,50);
//               mediaPlayerComponent.setSize(700, 500);
//           }
//
//           frame.setContentPane(mediaPlayerComponent);
//           frame.setVisible(true);
//
//           
//           EmbeddedMediaPlayer mediaPlayer = mediaPlayerComponent.getMediaPlayer();
//           mediaPlayer.setStandardMediaOptions();
//           mediaPlayer.playMedia("rtmp://34.227.142.101/myapp/test");   
    
//        final String url = "rtmp://54.197.9.24:1935/live";
//        final String stream = "desktop";
//        
//        final DesktopCamera camera = new DesktopCamera(0 /*x*/, 0 /*y*/, Toolkit.getDefaultToolkit().getScreenSize().width /*width*/, Toolkit.getDefaultToolkit().getScreenSize().height /*height*/);
//        
//        new Thread(new Runnable()
//        {
//            public void run()
//            {
//                final Publisher publisher = new Publisher();
//                
//                // RTMP example
//                publisher.publish(new com.smaxe.uv.client.rtmp.NetConnection(), url, stream, null /*microphone*/, camera);
//                
//                // RTMFP example
//                // Note: This classes are available in the JUV RTMFP/RTMP Client library (juv-rtmfp-client-*.jar)
//                // com.smaxe.uv.client.rtmfp.License.setKey("SET-YOUR-KEY");
//                // 
//                // publisher.publish(new com.smaxe.uv.client.rtmfp.NetConnection(), url, stream, null /*microphone*/, camera);
//            }
//        }, "ExRtmpDesktopPublisher-Publisher").start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * <code>DesktopCamera</code> - {@link ICamera} implementation that captures desktop.
     * 
     * @author Andrei Sochirca
     */
    public final static class DesktopCamera extends AbstractCamera
    {
        /**
         * <code>CaptureRunnable</code> - {@link Runnable} implementation
         * that captures desktop.
         */
        private final class CaptureRunnable extends Object implements Runnable
        {
            // fields
            private volatile int x = 0;
            private volatile int y = 0;
            private volatile int width = 320;
            private volatile int height = 240;
            
            private volatile boolean active = true;
            
            private Deflater deflater = new Deflater();
            
            /**
             * Constructor.
             * 
             * @param x
             * @param y
             * @param width
             * @param height
             */
            public CaptureRunnable(final int x, final int y, final int width, final int height)
            {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }
            
            /**
             * Sets the origin.
             * 
             * @param x
             * @param y
             */
            public void setOrigin(final int x, final int y)
            {
                this.x = x;
                this.y = y;
            }
            
            /**
             * Releases the capture resources.
             */
            public void release()
            {
                active = false;
            }
            
            // Runnable implementation
            
            public void run()
            {
                final int blockWidth = 64;
                final int blockHeight = 64;
                final float frameRate = 60.f;
                
                long frames = 0;
                
                try
                {
                    final Robot robot = new Robot();
                    
                    final long itime = System.nanoTime();
                    
                    long duration = 0;
                    int[] prgb = null;
                    
                    while (active)
                    {
                        final long ctime = System.nanoTime();
                        final long mediaTimestamp = (ctime - itime) / 1000000;
                        
                        try
                        {
//                            System.out.println("size1: " + image.s);
                            // The important part: Create in-memory stream
//                            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
//                            ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);
//
//                            // NOTE: The rest of the code is just a cleaned up version of your code
//
//                            // Obtain writer for JPEG format
//                            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
//
//                            // Configure JPEG compression: 70% quality
//                            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
//                            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//                            jpgWriteParam.setCompressionQuality(0.3f);
//
//                            // Set your in-memory stream as the output
//                            jpgWriter.setOutput(outputStream);
//
//                            // Write image as JPEG w/configured settings to the in-memory stream
//                            // (the IIOImage is just an aggregator object, allowing you to associate
//                            // thumbnails and metadata to the image, it "does" nothing)
//                            jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
//
//                            // Dispose the writer to free resources
//                            jpgWriter.dispose();
//
//                            // Get data for further processing...
//                            byte[] jpegData = compressed.toByteArray();
//                            
//                            System.out.println("size: " + jpegData.length);
                            
//                            DataBuffer buf = new DataBuffer(jpegData);
                            BufferedImage image = Thumbnails.of(robot.createScreenCapture(new Rectangle(x, y, width, height))).size(width / 2, height / 2).asBufferedImage();
                            final int[] rgb = toRGB(image);
                            final byte[] packet = encode(rgb, prgb, width / 2, height / 2, blockWidth, blockHeight);
                            
                            if (packet != null)
                            {
                                System.out.println(packet.length);
                                fireOnVideoData(MediaDataFactory.create((int) (mediaTimestamp - duration), mediaTimestamp,
                                        new ByteArray(packet)));
                            }
                            
                            duration = mediaTimestamp;
                            prgb = rgb;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        
                        if (frames++ > 0)
                        {
                            Thread.sleep(Math.max((int) (((frames - 1) * 1000000000d / frameRate) - ctime + itime) / 1000000, 10));
                        }
                        
                        if (frames % 20 == 0) prgb = null;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            // inner use methods
            /**
             * Encodes the frame.
             * 
             * @param rgb frame rgb
             * @param width frame width
             * @param height frame height
             * @param prgb previous rgb data
             * @return encoded frame bytes, <code>null</code> if frame wasn't changed
             * @throws Exception if an exception occurred
             */
            private byte[] encode(int[] rgb, int[] prgb, final int width, final int height, final int blockWidth, final int blockHeight) throws Exception
            {
                if (prgb != null && prgb.length != rgb.length) prgb = null;
                
                boolean isKeyFrame = true;
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
                
                // tag byte will be replaced later
                baos.write(0 /* tag */);
                
                // write header
                final int wh = width + ((blockWidth / 16 - 1) << 12);
                final int hh = height + ((blockHeight / 16 - 1) << 12);
                
                writeShort(baos, wh);
                writeShort(baos, hh);
                
                // write content
                int y0 = height;
                int x0 = 0;
                int bwidth = blockWidth;
                int bheight = blockHeight;
                byte[] buf = new byte[3 * blockWidth];
                int changedBlocks = 0;
                
                while (y0 > 0)
                {
                    bheight = Math.min(y0, blockHeight);
                    y0 -= bheight;
                    
                    bwidth = blockWidth;
                    x0 = 0;
                    
                    while (x0 < width)
                    {
                        bwidth = (x0 + blockWidth > width) ? width - x0 : blockWidth;
                        
                        final boolean changed = isImageBlockChanged(rgb, prgb, width, height, x0, y0, bwidth, bheight);
                        
                        if (changed)
                        {
                            changedBlocks++;
                            
                            ByteArrayOutputStream blaos = new ByteArrayOutputStream(4 * 1024);
                            
                            DeflaterOutputStream dos = new DeflaterOutputStream(blaos, deflater);
                            
                            for (int y = 0; y < bheight; y++)
                            {
                                for (int offset = (y0 + bheight - y - 1) * width + x0, i = 0; i < bwidth; i++)
                                {
                                    int pixel = rgb[offset + i];
                                    
                                    buf[3 * i + 0] = (byte) (pixel & 0xFF);
                                    buf[3 * i + 1] = (byte) ((pixel >> 8) & 0xFF);
                                    buf[3 * i + 2] = (byte) ((pixel >> 16) & 0xFF);
                                }
                                
                                dos.write(buf, 0, 3 * bwidth);
                            }
                            
                            dos.finish();
                            deflater.reset();
                            
                            final byte[] bbuf = blaos.toByteArray();
                            final int written = bbuf.length;
                            
                            // write DataSize
                            writeShort(baos, written);
                            // write Data
                            baos.write(bbuf, 0, written);
                        }
                        else
                        {
                            isKeyFrame = false;
                            // write DataSize
                            writeShort(baos, 0);
                        }
                        
                        x0 += bwidth;
                    }
                }
                
                if (changedBlocks == 0) return null;
                
                byte[] data = baos.toByteArray();
                
                data[0] = (byte) getTag(isKeyFrame ? 0x01 /*key-frame*/ : 0x02 /*inter-frame*/, 0x03 /*ScreenVideo codec*/);
                
                return data;
            }
            
            /**
             * Writes short value to the {@link OutputStream <tt>os</tt>}.
             * 
             * @param os
             * @param n
             * @throws Exception if an exception occurred
             */
            private void writeShort(OutputStream os, final int n) throws Exception
            {
                os.write((n >> 8) & 0xFF);
                os.write((n >> 0) & 0xFF);
            }
            
            /**
             * @param frame
             * @param codec
             * @return tag
             */
            private int getTag(final int frame, final int codec)
            {
                return ((frame & 0x0F) << 4) + ((codec & 0x0F) << 0);
            }
            
            /**
             * Checks if image block is changed.
             * 
             * @param rgb current RGB frame
             * @param prgb current RGB frame
             * @param width frame width
             * @param height frame height
             * @param x0
             * @param y0
             * @param blockWidth
             * @param blockHeight
             * @return <code>true</code> if changed, otherwise <code>false</code>
             */
            private boolean isImageBlockChanged(int[] rgb, int[] prgb,
                    int width, int height, int x0, int y0, int blockWidth, int blockHeight)
            {
                if (prgb == null) return true;
                
                for (int y = Math.min(y0 + blockHeight - 1, height - 1); y >= y0; y--)
                {
                    for (int x = x0, xn = Math.min(x0 + blockHeight, width); x < xn; x++)
                    {
                        final int off = x + width * y;
                        
                        if (rgb[off] != prgb[off]) return true;
                    }
                }
                
                return false;
            }
            
            /**
             * @param image
             * @return RGB image content
             */
            private int[] toRGB(BufferedImage image)
            {
                return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            }
        }
        
        // fields
        private CaptureRunnable capture = null;
        private Thread t = null;
        
        /**
         * Constructor.
         */
        public DesktopCamera()
        {
            this(0 /*x*/, 0 /*y*/, 320 /*width*/, 240 /*height*/);
        }
        
        /**
         * Constructor.
         * 
         * @param x
         * @param y
         * @param width
         * @param height
         */
        public DesktopCamera(final int x, final int y, final int width, final int height)
        {
            capture = new CaptureRunnable(x, y, width, height);
        }
        
        /**
         * Starts desktop capture.
         */
        public void start()
        {
            if (t == null)
            {
                t = new Thread(capture, "ExRtmpDesktopPublisher-DesktopCamera");
                t.start();
            }
        }
        
        /**
         * Releases the resources.
         */
        public void release()
        {
            capture.release();
            t = null;
        }
    }
    
    /**
     * <code>Publisher</code> - publisher.
     */
    public static final class Publisher extends Object
    {
        /**
         * <code>NetConnectionListener</code> - {@link INetConnection} listener implementation.
         */
        private final class NetConnectionListener extends INetConnection.ListenerAdapter
        {
            /**
             * Constructor.
             */
            public NetConnectionListener()
            {
            }
            
            @Override
            public void onAsyncError(final INetConnection source, final String message, final Exception e)
            {
                System.out.println("Publisher#NetConnection#onAsyncError: " + message + " " + e);
            }
            
            @Override
            public void onIOError(final INetConnection source, final String message)
            {
                System.out.println("Publisher#NetConnection#onIOError: " + message);
            }
            
            @Override
            public void onNetStatus(final INetConnection source, final Map<String, Object> info)
            {
                System.out.println("Publisher#NetConnection#onNetStatus: " + info);
                
                final Object code = info.get("code");
                
                if (INetConnection.CONNECT_SUCCESS.equals(code))
                {
                }
                else
                {
                    disconnected = true;
                }
            }
        }
        
        // fields
        private volatile boolean disconnected = false;
        
        /**
         * Publishes the stream.
         * 
         * @param connection
         * @param url
         * @param streamName
         * @param microphone microphone
         * @param camera camera
         */
        public void publish(final INetConnection connection, final String url, final String streamName, final IMicrophone microphone, final DesktopCamera camera)
        {
            connection.addEventListener(new NetConnectionListener());
            
            connection.connect(url);
            
            // wait till connected
            while (!connection.connected() && !disconnected)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (Exception e) {/*ignore*/}
            }
            
            if (!disconnected)
            {
                final INetStream stream = connection.createNetStream();
                
                stream.addEventListener(new INetStream.ListenerAdapter()
                {
                    @Override
                    public void onNetStatus(final INetStream source, final Map<String, Object> info)
                    {
                        System.out.println("Publisher#NetStream#onNetStatus: " + info);
                        
                        final Object code = info.get("code");
                        
                        if (INetStream.PUBLISH_START.equals(code))
                        {
                            if (microphone != null)
                            {
                                stream.attachAudio(microphone);
                            }
                            
                            if (camera != null)
                            {
                                stream.attachCamera(camera, -1 /*snapshotMilliseconds*/);
                                
                                camera.start();
                            }
                        }
                    }
                });
                
                
                stream.publish(streamName, INetStream.LIVE);
                
                while (!disconnected)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {/*ignore*/}
                }
            }
            
            connection.close();
            camera.release();
        }
    }
}