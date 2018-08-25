本文内容涉及同步与异步, 阻塞与非阻塞, BIO、NIO、AIO等概念, 这块内容本身比较复杂, 很难用三言两语说明白. 而书上的定义更不容易理解是什么意思. 下面跟着我一起解开它们神秘的面纱。
<!-- more -->
- BIO    传统的socket编程，属于同步阻塞模型
- NIO 官方(new io) jdk1.4推出 俗称(non-block io) ，属于同步非阻塞模式
- AIO  又称NIO2.0在jdk1.7推出，属于异步非阻塞模式
#解读同步异步，阻塞非阻塞。

### 阻塞和非阻塞

从简单的开始，我们以经典的读取文件的模型举例。（对操作系统而言，所有的输入输出设备都被抽象成文件。）

在发起读取文件的请求时，应用层会调用系统内核的I/O接口。

如果应用层调用的是阻塞型I/O，那么在调用之后，应用层即刻被挂起，一直出于等待数据返回的状态，直到系统内核从磁盘读取完数据并返回给应用层，应用层才用获得的数据进行接下来的其他操作。

如果应用层调用的是非阻塞I/O，那么调用后，系统内核会立即返回（虽然还没有文件内容的数据），应用层并不会被挂起，它可以做其他任意它想做的操作。（至于文件内容数据如何返回给应用层，这已经超出了阻塞和非阻塞的辨别范畴。）

这便是（脱离同步和异步来说之后）阻塞和非阻塞的区别。总结来说，是否是阻塞还是非阻塞，关注的是接口调用（发出请求）后等待数据返回时的状态。被挂起无法执行其他操作的则是阻塞型的，可以被立即「抽离」去完成其他「任务」的则是非阻塞型的。
![image](http://upload-images.jianshu.io/upload_images/5338436-7efe3dbe5cc4a89f.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



### 同步和异步

阻塞和非阻塞解决了应用层等待数据返回时的状态问题，那系统内核获取到的数据到底如何返回给应用层呢？这里不同类型的操作便体现的是同步和异步的区别。

对于同步型的调用，应用层需要自己去向系统内核问询，如果数据还未读取完毕，那此时读取文件的任务还未完成，应用层根据其阻塞和非阻塞的划分，或挂起或去做其他事情（所以同步和异步并不决定其等待数据返回时的状态）；如果数据已经读取完毕，那此时系统内核将数据返回给应用层，应用层即可以用取得的数据做其他相关的事情。

而对于异步型的调用，应用层无需主动向系统内核问询，在系统内核读取完文件数据之后，会主动通知应用层数据已经读取完毕，此时应用层即可以接收系统内核返回过来的数据，再做其他事情。

这便是（脱离阻塞和非阻塞来说之后）同步和异步的区别。也就是说，是否是同步还是异步，关注的是任务完成时消息通知的方式。由调用方盲目主动问询的方式是同步调用，由被调用方主动通知调用方任务已完成的方式是异步调用。

![image](http://upload-images.jianshu.io/upload_images/5338436-bb181c8d4025ea5e.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

假设小明需要在网上下载一个软件：

如果小明点击下载按钮之后，就一直干瞪着进度条不做其他任何事情直到软件下载完成，这是同步阻塞；
如果小明点击下载按钮之后，就一直干瞪着进度条不做其他任何事情直到软件下载完成，但是软件下载完成其实是会「叮」的一声通知的（但小明依然那样干等着），这是异步阻塞；（不常见）
如果小明点击下载按钮之后，就去做其他事情了，不过他总需要时不时瞄一眼屏幕看软件是不是下载完成了，这是同步非阻塞；
如果小明点击下载按钮之后，就去做其他事情了，软件下载完之后「叮」的一声通知小明，小明再回来继续处理下载完的软件，这是异步非阻塞。
相信看完以上这个案例之后，这几个概念已经能够分辨得很清楚了。

总的来说，**同步和异步关注的是任务完成消息通知的机制，而阻塞和非阻塞关注的是等待任务完成时请求者的状态。**
# java网络编程
  我们通过 客户端像服务端查询信息作为一个例子。分别通过三种模型来实现。

## 1.1、传统的BIO
在传统的网络编程中，服务端监听端口，客户端请求服务端的ip跟监听的端口，跟服务端通信，必须三次握手建立。如果连接成功，通过套接字（socket）进行通信。
   在BIO通信模型：采用BIO通信模型的服务端，通常由一个独立的Acceptor线程负责监听客户端的连接，它接收到客户端连接请求之后为每个客户端创建一个新的线程进行链路处理没处理完成后，通过输出流返回应答给客户端，线程销毁。即典型的一请求一应答通信模型。
![image.png](https://upload-images.jianshu.io/upload_images/5338436-925e9372a9ee5071.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
从图中可以得知，该模型中每一个请求对应一个线程处理，在线程数量有限的情况下，请求数量多，那么服务器就会因为资源不足而挂掉。
服务端代码
```java
/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 * 服务端
 */
public class Server {

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6789;

    public  void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public  void start(Integer port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("小yu机器人启动，监听端口为：" + port);
        //通过无线循环监听客户端连接
        while (true) {
            // 阻塞方法，直至有客户端连接成功
            Socket socket = serverSocket.accept();
            // 多线程处理客户端请求
            new Thread(new ServerHandler(socket)).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }


}
```

服务端处理器代码
```
/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 *  服务端业务逻辑处理器
 */
public class ServerHandler implements Runnable{

    private Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // 获取socket的字符缓存输入流 也就是获取客户端给服务器的字符流
            in = new BufferedReader( new InputStreamReader(this.socket.getInputStream()));
            // 获取socket的字符输出流 也就是发送的客户的字符流 第二个参数自动刷新
            out = new PrintWriter( new OutputStreamWriter(this.socket.getOutputStream()), true);
            String request, response;
            // 读取输入流的消息 如果为空 则退出读取
            while ((request = in.readLine()) != null) {
                System.out.println("[" + Thread.currentThread().getName()+ "]" + "小yu机器人收到消息：" + request);
                // 具体业务逻辑处理 查询信息。
                response = ResponseUtil.queryMessage(request);
                out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            // 资源释放
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }


}
```
客户端代码
```java
/**
 * @author yukong
 * @date 2018年8月24日18:51:40
 * 客户端
 */
public class Client {

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6789;

    /**
     * 默认端口
     */
    private static final String DEFAULT_HOST = "localhost";

    public  void send(String key){
        send(DEFAULT_PORT,key);
    }
    public  void send(int port,String key){
        System.out.println("查询的key为：" + key);
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try{
            socket = new Socket(DEFAULT_HOST,port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            out.println(key);
            System.out.println("查询的结果为：" + in.readLine());
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            if(out != null){
                out.close();
                out = null;
            }
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client();
        while (scanner.hasNext()) {
            String key = scanner.next();
            client.send(key);
        }
    }

}
```
从代码中可以得知，我们每次请求都是new Thread去处理，意味着线程消耗巨大，可能会有朋友说道，那就用线程池，同样的如果使用线程池，当达到线程最大数量，也会达到瓶颈。该模式不适合高并发的访问。

## 1.2 NIO模型

    NIO提供了与传统BIO模型中的Socket和ServerSocket相对应的SocketChannel和ServerSocketChannel两种不同的套接字通道实现。
  新增的着两种通道都支持阻塞和非阻塞两种模式。
 阻塞模式使用就像传统中的支持一样，比较简单，但是性能和可靠性都不好；非阻塞模式正好与之相反。
对于低负载、低并发的应用程序，可以使用同步阻塞I/O来提升开发速率和更好的维护性；对于高负载、高并发的（网络）应用，应使用NIO的非阻塞模式来开发。
下面会先对基础知识进行介绍。

###     1.2.1、缓冲区 Buffer
  Buffer是一个对象，包含一些要写入或者读出的数据。

 在NIO库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区中的；在写入数据时，也是写入到缓冲区中。任何时候访问NIO中的数据，都是通过缓冲区进行操作。
 缓冲区实际上是一个数组，并提供了对数据结构化访问以及维护读写位置等信息。
   具体的缓存区有这些：ByteBuffe、CharBuffer、 ShortBuffer、IntBuffer、LongBuffer、FloatBuffer、DoubleBuffer。他们实现了相同的接口：Buffer。

###     1.2.2、通道 Channel

  我们对数据的读取和写入要通过Channel，它就像水管一样，是一个通道。通道不同于流的地方就是通道是双向的，可以用于读、写和同时读写操作。
  底层的操作系统的通道一般都是全双工的，所以全双工的Channel比流能更好的映射底层操作系统的API。
 Channel主要分两大类：

*   SelectableChannel：用户网络读写
*   FileChannel：用于文件操作

    后面代码会涉及的ServerSocketChannel和SocketChannel都是SelectableChannel的子类。

###    1. 2.3、多路复用器 Selector
 Selector是Java  NIO 编程的基础。
Selector提供选择已经就绪的任务的能力：Selector会不断轮询注册在其上的Channel，如果某个Channel上面发生读或者写事件，这个Channel就处于就绪状态，会被Selector轮询出来，然后通过SelectionKey可以获取就绪Channel的集合，进行后续的I/O操作。
 一个Selector可以同时轮询多个Channel，因为JDK使用了epoll()代替传统的select实现，所以没有最大连接句柄1024/2048的限制。所以，只需要一个线程负责Selector的轮询，就可以接入成千上万的客户端。
服务端代码
```java
/**
 * 服务端
 */
public class Server {


    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6780;

    public  void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public  void start(Integer port) throws IOException {
        // 打开多路复用选择器
        Selector selector = Selector.open();
        //  打开服务端监听通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 绑定监听的端口
        serverSocketChannel.bind(new InetSocketAddress(port));
        // 将选择器绑定到监听信道,只有非阻塞信道才可以注册选择器.并在注册过程中指出该信道可以进行Accept操作
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("小yu机器人启动，监听端口为：" + port);
        new Thread(new ServerHandler(selector)).start();
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

}
```
服务端处理器
```java
public class ServerHandler implements Runnable{

    private Selector selector;

    public ServerHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
        while (true) {
            // 等待某信道就绪(或超时)
            if(selector.select(1000)==0){
//                System.out.print("独自等待.");
                continue;
            }
            // 取得迭代器.selectedKeys()中包含了每个准备好某一I/O操作的信道的SelectionKey
            Iterator<SelectionKey> keyIterator=selector.selectedKeys().iterator();
           while (keyIterator.hasNext()){
               SelectionKey sk = keyIterator.next();
               // 删除已选的key 以防重负处理
               keyIterator.remove();
               // 处理key
               handlerSelect(sk);

            }

        }


       } catch (IOException e) {

       }
    }

    private void handlerSelect(SelectionKey sk) throws IOException {
        // 处理新接入的请求
        if (sk.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) sk.channel();
            //通过ServerSocketChannel的accept创建SocketChannel实例
            //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
            SocketChannel sc = ssc.accept();
            //设置为非阻塞的
            sc.configureBlocking(false);
            //注册为读
            sc.register(selector, SelectionKey.OP_READ);
        }
        // 读操作
        if (sk.isReadable()) {
            String request, response;
            SocketChannel sc = (SocketChannel) sk.channel();
            // 创建一个ByteBuffer 并设置大小为1m
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            // 获取到读取的字节长度
            int readBytes = sc.read(byteBuffer);
            // 判断是否有数据
            if (readBytes > 0) {
                //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                byteBuffer.flip();
                //根据缓冲区可读字节数创建字节数组
                byte[] bytes = new byte[byteBuffer.remaining()];
                // 复制至新的缓冲字节流
                byteBuffer.get(bytes);
                request = new String(bytes, "UTF-8");
                System.out.println("[" + Thread.currentThread().getName()+ "]" + "小yu机器人收到消息：" + request);
                // 具体业务逻辑处理 查询信息。
                response = ResponseUtil.queryMessage(request);
                //将消息编码为字节数组
                byte[] responseBytes = response.getBytes();
                //根据数组容量创建ByteBuffer
                ByteBuffer writeBuffer = ByteBuffer.allocate(responseBytes.length);
                //将字节数组复制到缓冲区
                writeBuffer.put(responseBytes);
                //flip操作
                writeBuffer.flip();
                //发送缓冲区的字节数组
                sc.write(writeBuffer);
            }
        }
    }


}
```
客户端
```java
/**
 * 客户端
 */
public class Client {


    // 通道选择器
    private Selector selector;

    // 与服务器通信的通道
    SocketChannel socketChannel;

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6780;

    /**
     * 默认端口
     */
    private static final String DEFAULT_HOST = "127.0.0.1";

    public  void send(String key) throws IOException {
        send(DEFAULT_PORT, DEFAULT_HOST, key);
    }
    public  void send(int port,String host, String key) throws IOException {
        init(port, host);
        System.out.println("查询的key为：" + key);
        //将消息编码为字节数组
        byte[] bytes = key.getBytes();
        //根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        //将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        //flip操作
        writeBuffer.flip();
        //发送缓冲区的字节数组
        socketChannel.write(writeBuffer);
        //****此处不含处理“写半包”的代码
    }

    public void init(int port,String host) throws IOException {
        // 创建选择器
        selector = Selector.open();

        // 设置链接的服务端地址
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        // 打开通道
        socketChannel = SocketChannel.open(socketAddress);// 非阻塞
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new ClientHandler(selector)).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client();
        while (scanner.hasNext()) {
            String key = scanner.next();
            client.send(key);
        }
    }

}
```
客户端处理器
```java
public class ClientHandler implements Runnable {

    private Selector selector;

    public ClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if(selector.select(1000) < 0) {
                 continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = keys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey sc = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    //读消息
                    if (sc.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) sc.channel();
                        //创建ByteBuffer，并开辟一个1M的缓冲区

                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        //读取请求码流，返回读取到的字节数
                        int byteSize = socketChannel.read(byteBuffer);
                        if (byteSize > 0) {
                            //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                            byteBuffer.flip();
                            //根据缓冲区可读字节数创建字节数组 总长度减去空余的
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            // 复制至新的缓冲字节流
                            byteBuffer.get(bytes);
                            String message = new String(bytes, "UTF-8");
                            System.out.println(message);
                        }
                    }
                }
            }
        } catch (IOException e) {

        }
    }
}

```
从代码中 我们也能看出来，nio解决的是阻塞与非阻塞的，通过selector轮询上注册的channel的状态，来获取对应准备就绪channel的 那么请求者就不用一直去accpet阻塞，等待了。那为什么是同步呢，因为还是我们请求者不停的轮询selector是否有完全就绪的channel。

# 3、AIO编程
   NIO 2.0引入了新的异步通道的概念，并提供了异步文件通道和异步套接字通道的实现。
   异步的套接字通道时真正的异步非阻塞I/O，对应于UNIX网络编程中的事件驱动I/O（AIO）。他不需要过多的Selector对注册的通道进行轮询即可实现异步读写，从而简化了NIO的编程模型。
服务端代码
```java
/**
 *  异步非阻塞服务端
 */
public class Sever {

    /**
     * 默认端口
     */
    private static final Integer DEFAULT_PORT = 6780;

    private AsynchronousServerSocketChannel serverChannel;


    //作为handler接收客户端连接
    class ServerCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

        private AsynchronousServerSocketChannel serverChannel;
        private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        private CharBuffer charBuffer;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

        public ServerCompletionHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }


        @Override
        public void completed(AsynchronousSocketChannel result, Void attachment) {

            //立即接收下一个请求,不停顿
            serverChannel.accept(null, this);
            try {
                while (result.read(buffer).get() != -1) {
                    buffer.flip();
                    charBuffer = decoder.decode(buffer);
                    String request = charBuffer.toString().trim();
                    System.out.println("[" + Thread.currentThread().getName()+ "]" + "小yu机器人收到消息：" + request);
                    // 具体业务逻辑处理 查询信息。
                    String response = ResponseUtil.queryMessage(request);
                    //将消息编码为字节数组
                    byte[] responseBytes = response.getBytes();
                    //根据数组容量创建ByteBuffer
                    ByteBuffer outBuffer = ByteBuffer.allocate(responseBytes.length);
                    //将字节数组复制到缓冲区
                    outBuffer.put(responseBytes);
                    //flip操作
                    outBuffer.flip();
                    //发送缓冲区的字节数组
                    result.write(outBuffer).get();
                    if (buffer.hasRemaining()) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            } finally {
                try {
                    result.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            //立即接收下一个请求,不停顿
            serverChannel.accept(null, this);
            throw new RuntimeException("connection failed!");
        }

    }


    public void init() throws IOException, InterruptedException {
        init(DEFAULT_PORT);
    }

    public void init(Integer port) throws IOException, InterruptedException {
        // 打开异步通道
        this.serverChannel = AsynchronousServerSocketChannel.open();
        // 判断通道是否打开
        if (serverChannel.isOpen()) {
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(port));
        } else {
            throw new RuntimeException("Channel not opened!");
        }
        start(port);
    }

    public void start(Integer port) throws InterruptedException {
        System.out.println("小yu机器人启动，监听端口为：" + port);
        this.serverChannel.accept(null, new ServerCompletionHandler(serverChannel));
        // 保证线程不会挂了
        while (true) {
            Thread.sleep(5000);
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Sever server = new Sever();
        server.init();
    }
}

```
客户端
```java
public class Client {

    class ClientCompletionHandler implements CompletionHandler<Void, Void> {

        private AsynchronousSocketChannel channel;
        private CharBuffer charBufferr = null;
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
        private BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

        public ClientCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }


        @Override
        public void completed(Void result, Void attachment) {

            System.out.println("Input Client Reuest:");
            String request;
            try {
                request = clientInput.readLine();
                channel.write(ByteBuffer.wrap(request.getBytes()));
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                while(channel.read(buffer).get() != -1){
                    buffer.flip();
                    charBufferr = decoder.decode(buffer);
                    System.out.println(charBufferr.toString());
                    if(buffer.hasRemaining()){
                        buffer.compact();
                    }
                    else{
                        buffer.clear();
                    }
                    request = clientInput.readLine();
                    channel.write(ByteBuffer.wrap(request.getBytes())).get();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            throw  new  RuntimeException("channel not opened!");
        }

    }

    public void start() throws IOException, InterruptedException{
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        if(channel.isOpen()){
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 128*1024);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 128*1024);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE,true);
            channel.connect(new InetSocketAddress("127.0.0.1",6780),null,new ClientCompletionHandler(channel));
            while(true){
                Thread.sleep(5000);
            }
        }
        else{
            throw new RuntimeException("Channel not opened!");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Client client = new Client();
        client.start();
    }


}
```
# 4、各种I/O的对比
   先以一张表来直观的对比一下：
![image.png](https://upload-images.jianshu.io/upload_images/5338436-5bfd7dae71de28cb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 5 
References
[完全理解同步/异步与阻塞/非阻塞](https://zhuanlan.zhihu.com/p/22707398)
[Java 网络IO编程总结](https://blog.csdn.net/anxpp/article/details/51512200)
下面是配套的完整代码地址
[完整代码地址](https://github.com/YuKongEr/io-study)


