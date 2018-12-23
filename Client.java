import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

//import java.time.Instant;
//import java.time.format.DateTimeFormatter;

public class Client {

	private Socket cli_sock; // Client side socket
	private String lnsep = System.getProperty("line.separator"); // This ensures that the line separator works on every system, and not just Windows.
	private PrintWriter outpt; // Output to the Server
	private FileWriter logpt; // Used to write to the file
	private	Instant insta_start; //Taking an instant in time
	private	Instant insta_end; // And another one
	private DateTimeFormatter form; //Date_and_Time //Logging date and time
	private LocalDateTime dnt;
	private BufferedReader srvIn; // Receive information from the sever through this reader
	private BufferedReader cliIn; // Input from the terminal
	
	
	public Client(String ip, int port) throws InterruptedException   
	{
		try 
		{
			logpt = new FileWriter("client.log",true); // Used to type to the server
			form   = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); // Using the same date and time formatting as the first one
			dnt = LocalDateTime.now(); // Date and time instantiation, starting now
			try
			{
			cli_sock = new Socket(ip,port); //Creates the client side socket with the specified port and IP( 42069 and 127.0.0.1(local host))
			}
			catch(NullPointerException e)
			{
				System.out.println("There has been an error creating the Socket. IP Adress is NULL. Please restart the Client.");
				e.printStackTrace();
			}
			catch (IOException e)
			{
				System.out.println("There has been an error in creating the Socket. Generic IOException. Server port probably busy. Please restart both and check again.");
				e.printStackTrace();
				
			}
			outpt = new PrintWriter(cli_sock.getOutputStream());// Shoots info to server
			cliIn = new BufferedReader(new InputStreamReader(System.in)); //Takes input from terminal
			srvIn = new BufferedReader(new InputStreamReader(cli_sock.getInputStream()));
			
		}
		catch(UnknownHostException  e) //Invalid user/Server down
		{
			e.printStackTrace();
			System.out.println("Couldn't recognise host.");
			
		}
		catch(ConnectException e)
		{
			
			System.out.println("An ConnectException has occured.Server probably not up yet.");
			
		}
		
		catch(SocketException e)
		{
			e.printStackTrace();
			System.out.println("This is a socket error, probably teh server is down. Sorry about that! You prick!");
		}
		catch(InterruptedIOException e)
		{
			e.printStackTrace();
			System.out.println("This is an InterruptedIOException, Sorry about that. ");
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("Generic IOException, Check StackTrace!");
		}
		
			
		
	}
	
	
	public void close() // Used to close all the files to stop any memory leakage/overflow
	{
		try {
			cli_sock.close();
			outpt.close();
			cliIn.close();
			srvIn.close();
			logpt.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void sendRequest()
	{
		outpt.println("Client attempted to connect to the server at time: " + form.format(dnt));
		outpt.flush();
		String line_rd = ""; // Used to read input from the client 
		System.out.println("Please input the name of an artist whose songs you wish to find.");
        try 
        {
        	insta_start = Instant.now(); // Connection start time recorded
			line_rd = cliIn.readLine();
			
		} 
        catch (IOException e)
        {
			System.out.println("A problem has occured in receiving the response from the server. Please restart and try again.");
			e.printStackTrace();
		}
        outpt.println(line_rd); 
        outpt.flush(); // Empty the buffer to the stream!
        
	}
	
	public void receiveRequest()
	{
		try 
		{
			insta_end = Instant.now();
			long reqTime = insta_end.toEpochMilli() - insta_start.toEpochMilli(); // The amount of time it took the from response send to now
			String ret = srvIn.readLine();
			System.out.println("You have received this message from the server: " + ret); // This would be the song associated with the artist. I'm not completely sure that I will achieve this.
			logpt.append(lnsep + "The server response time was: " +  reqTime +"sec with the response of : " + ret);
			logpt.append(lnsep + "The server response was " + ret.getBytes().length + "bytes long");
			logpt.append(lnsep + "The server response was received at time : " +form.format(dnt) );
			
		} 
		catch (IOException e)
		{
			System.out.println("Something went wronmg with receiving the request. Generic IOException thrown.");
			e.printStackTrace();
		}
		
		
	}
	
	
	public void quit() // This method will be used to inform the client that he/she should quit from the server
	{
		System.out.println("Exchange between the server and the client has come to and end.");
		System.out.println("When you are ready, type in quit in order to exit the program");
		String q = "";
		do
		{
			try 
			{
				
				q = cliIn.readLine();
				outpt.println(q);
				outpt.flush();
				insta_end = Instant.now(); // Recorded time of end of connection 
				
			} 
			catch (IOException e)
			{
				System.out.println("There has been some sort of problem with the quitting.");
				e.printStackTrace();
			}
		}
		while(!q.equalsIgnoreCase("quit"));
		System.out.println("Connection has been severed. Closing in 2 seconds");
		try 
		{
			Thread.sleep(2000);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String args[]) throws InterruptedException
	{
				Client client = new Client("127.0.0.1",42069);
				client.sendRequest();
				client.receiveRequest();
				client.quit();
				client.close();
			
	}	
	
}
