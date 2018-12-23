import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.*; // This will be used for pattern recognition in the text

public class Server {

	private Socket cli_soc; // The socket which is going to link with the client socket
	private ServerSocket srv_soc; // A server socket to provide the server side client socket
	private BufferedReader inpt; // Gets input from the client
	private PrintWriter outpt; //Output back to client
	private String lnsep = System.getProperty("line.separator"); // This ensures that the line separator works on every system, and not just Windows.
	private HashMap<String,ArrayList<String>> song_list = new HashMap<String, ArrayList<String>>(); // The HashMap is instantiated here so that we may call the loadDatabase method right away
//	Logging stuff such as the FileWriter, Formater for the date and time, as well as Instants.
	
	private FileWriter logpt; // Used for logging stuff
	private DateTimeFormatter form; 
	private LocalDateTime dnt; //Date and time
	private Instant insta_start; //First instant from which we start measuring time
	private Instant insta_end; // The end point after which we stop measuring the time
	
	
	
	public Server(int port_num) // Instantiate the server.
	{
		try
		{
			this.loadDatabase("100worst.txt");
			form = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			logpt = new FileWriter("server.log",true); // Using this constructor so as to not delete the file every time
			try 
			{
				srv_soc = new ServerSocket(42069);
			} 
			catch (IOException e) 
			{
				System.out.println("Couldn't connect to the server socket. Port probably in use.");
				e.printStackTrace();
			} //Creates a new socket at the given port number(42069 in my case)
			dnt = LocalDateTime.now(); // Must go after the start of the server
			System.out.println("Waiting for a client to connect to server...");
			System.out.println("We got to here, that's good. Just before accepting the connection"); //works up to this point(I think)
			cli_soc = srv_soc.accept(); // Server has accepted the client socket.
			inpt = new BufferedReader(new InputStreamReader(cli_soc.getInputStream()));
			outpt = new PrintWriter(cli_soc.getOutputStream());
			logpt.append(lnsep + "-----------------------------------------------------------------------");
			logpt.flush();
			if( cli_soc == null)
			{
				logpt.append(lnsep + "Failed to connect to client at time " + form.format(dnt));
			}
			else logpt.append(lnsep + "The server has successfully connected to the client " + form.format(dnt)); // Log start of the server/client connection
			logpt.flush(); // Pushes the Buffer to the stream(and by extension the file)
			String cliResTime = "";
			cliResTime = inpt.readLine();
			System.out.println("The current value of clirestime" + cliResTime);
			logpt.append(lnsep + cliResTime);
			logpt.flush();
			insta_start = Instant.now(); // Take record of the start of the connection to the server
			System.out.println("Connection online");
			System.out.println("Client accepted!");
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("This is a general IOException for the server instantiation code.");
		}
			
		
	}
	
	
	public  void loadDatabase(String fname) 
	{
		if(fname.contains(".txt") == false) //Don't have to specify .txt anymore
		{
			fname.concat(".txt");
		}
		
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(fname));
				String songExp = "\\-(\\s)?[A-Za-z0-9'\\-&\\(\\)]((\\s)?[A-Za-z0-9'\\-&\\(\\)])*([A-Za-z0-9'&\\(\\)]+)";
				int countnt = 0; //Skip the first 6 lines of input
				StringBuilder sb = new StringBuilder();
				String line_rd = ""; //line_reader
				String artist = "art";
				String date = "dat";
				String song = "song";
				String pureSong = "sng";
				char[] rd_arr = null; // Used to reduce the length of statements in code
				
				do{
					if(countnt < 6)
					{
						countnt++;
						br.readLine();
					}
					else
					{
						line_rd = br.readLine();
						if(!checkInt(line_rd)) // If the line doesn't finish with a date
						{
							line_rd = line_rd + br.readLine();	
						}
//						sb.append(line_rd);
//						sb.append(lnsep);
						song = regUse(songExp,line_rd);
						System.out.println(song);
						pureSong = pureSong(song);
						System.out.println(pureSong);
						date = regUse("\\d{4}",line_rd);
						System.out.println(date);
						artist = line_rd.replace(song,"");
						System.out.println(artist);
						artist = artist.replace(date,"");
						System.out.println(artist);
						artist = artRemove(artist);
						System.out.println(artist);
						artist = normArt(artist);
						System.out.println(artist);
						if(song_list.containsKey(artist))
						{
							song_list.get(artist).add(pureSong);
						}
						else
						{
							ArrayList<String> arrStr = new ArrayList<String>();
							arrStr.add(pureSong);
							song_list.put(artist,arrStr);
						}	
//						
//						for ( String a : song_list.keySet() )
//						{
//							System.out.println(a);
//							for( ArrayList<String> b : )
//						}
						
						rd_arr = line_rd.toCharArray(); //Used below to stop at 100
						
						
					}
					if(rd_arr != null) // Check if the array is not null
					{
						if( rd_arr.length >= 3  ) // See if there are more than 3 in it
						{
							if((rd_arr[0] == '1' && rd_arr[1] == '0' && rd_arr[2] == '0')) // Check if line starts with 100, if it does, break
							{
								break;
							}
						}
					}
				}
				while(line_rd != null );
				String sb1 = sb.toString();
				System.out.println(sb1); //By this point sb1 contains the entire text file.
				
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("This is ment to print out the IOException related to line_rd. Check there in source");
		}
		
	}
	
	
	public static String regUse(String expression , String check) //Do regex. Use \\-(\\s)?[A-Za-z0-9'\\-&\\(\\)]((\\s)?[A-Za-z0-9'\\-&\\(\\)])*([A-Za-z0-9'&\\(\\)])+ for songs and \\d{4} to remove date at the end. 
	{
		Pattern regex = Pattern.compile(expression);
		Matcher regMat = regex.matcher(check);
		
		while(regMat.find())
		{
			if(regMat.group().length() != 0)
			{
				return regMat.group();
			}
		}
		return null;
	}
	
	public static boolean checkInt (String inp) // Checks whether or not the last is an integer ( used for the one which are in a new line) works
	{
		char[] inpArr = inp.toCharArray();
		int len = inpArr.length; 
		if(!isNum(inpArr[len-1]))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public static boolean isNum (char c) // Checks if the given character is a digit. Working
	{
		char[] list = "1234567890".toCharArray();
		for(char isChar : list)
		{
//			System.out.println("Testing : " + c +" vs " + isChar);
			if(c == isChar)
			{
				return true;
			}
		}
		return false;
	}
	
	
	public static String artRemove (String str) // A method that takes in multiples of artists and makes one for artist, and ensures that there is indeed only 1 artist. Works
	{
		char[] str_arr = str.toCharArray();
		int len = 0;
		
		if(str.contains("&") || str.contains("/"))
		{
			for(char e : str_arr)
			{
				if(e == '&' || e == '/')
				{
					break;
				}
				len++; //This will be the length up to the &
			}
			len+= 5; // in order to have some whitespace left after truncate
			char[] res = new char[len]; // Using len to construct new char[]
			
			for(int i=0;i<len-5;i++) //Reassigning to res
			{
				res[i] = str_arr[i];
			}
			res[len-5] = ' ';
			res[len-4] = ' ';
			res[len-3] = ' ';
			res[len-2] = ' ';
			res[len-1] = ' ';
			return new String(res);
		}	
		else if (str.contains(" and "))
		{
			for(int i = 0; i<str_arr.length;i++)
			{
				if(str_arr[i] == ' ' && str_arr[i+1] == 'a' && str_arr[i+2] == 'n' && str_arr[i+3] == 'd' && str_arr[i+4] == ' ' )
				{
					len = i;
				}
			}
			
			char[] res = new char[len];
			for(int i=0;i<len;i++)
			{
				res[i] = str_arr[i];
			}
			return new String(res);
		}
		else return str;
			
		
	}
	
	public static String pureSong(String str) //Method that returns the actual name of the song to use. Use with regex for song. Working
	{
		char[] str_arr = str.toCharArray();
		int len = 0;
		int j = 0;
		if(str_arr[1] == ' ')
		{
			len = str_arr.length-2;
			j = 2;
		}
		else 
		{
			len = str_arr.length-1;
			j = 1;
		}
		
		char [] res = new char[len];
		System.out.println(new String(res));
		for(int i = 0;i<len;i++)
		{
			res[i] = str_arr[i+j];
		}
		return new String(res);
		
	}
	
	
	public static String normArt (String str) // Takes the output of of artist when songUnpure and date are removed. This is of form n \\s* Artist name \\s*
	{
		char [] str_arr = str.toCharArray();
		int j= 0; // this int is used in order to normalise the two array sizes
		if(Character.isDigit(str_arr[2])) {j = 3;}
		else if(Character.isDigit(str_arr[1])){j = 2;}
		else j = 1;
		int len = str_arr.length-j;
		char [] res = new char[len];
		
		for(int i =0;i<len;i++)
		{
			res[i] = str_arr[i+j];
		
		}
		for(int i =0; i<len;i++)
		{
			if(Character.isLetter(res[i]) && Character.isWhitespace(res[i+1]) &&  Character.isLetter(res[i+2]))
			{
//				System.out.println("here");
				res[i+1] = '>'; //Using this character to remember where the whitespace is
			}
		}
		String strRes = new String(res);
		strRes = strRes.replace(" ", "");
		strRes = strRes.replace(">", " ");
		return strRes;
	
	}

	
	
	
	
	
	
	public void close()
	{
		try {
			cli_soc.close();
			srv_soc.close();
			inpt.close();
			outpt.close();
			logpt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void listen() //Yet to be tested still
	{
	
		try 
		{
			
			 //Takes input from client. Has to be instantiated here
			System.out.println("Here we should have a pause for input.");
			String artist =  "";
			artist = inpt.readLine(); // Gets the artist name from the client
			System.out.println("Name of artist is " + artist);
			logpt.append(lnsep + "Artist name "+ artist +" received at this point " + form.format(dnt));
			logpt.flush();
			System.out.println(artist);
			outpt.println(artist); //Returns the artist back to the client, soon to be replaced
			outpt.flush();
			srvRes(artist);
			
		
		
		
		}
		catch(IOException e)
		{
			//TODO deal with any errors which may occur here
			e.printStackTrace();
		}
		
	}
	
	public String srvRes(String artist) //Within this method the server will respond to the client with either a list of songs or a sorry message
	{
		ArrayList<String> l_artist; //The list of strings we get from the HashMap. Not entirely necessary but makes things a bit cleaner.
		String list_out = ""; //A representation of l_artist in order to send as a single string back to client
		if(!song_list.containsKey(artist)) //If the specified artist doesn't exist
		{
			outpt.println("This artist does not exist. Sorry about that!"); 
			outpt.flush();
		}
		else
		{
			l_artist = song_list.get(artist);
			for(String st:l_artist)
			{
				list_out = list_out + "," + st;
			}
			return list_out;
		}
		
		return null;
		
	}
	
	public void quit_srv () // This is going to be the quit condition on the servers side. It's going to wait for a response from the client to shut down
	{
		String quit = "";
		
		do
		{
			try 
			{
				quit = inpt.readLine();
				System.out.println(quit);
				insta_end = Instant.now(); //Take record of the ending of the connection
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
		
		}
		while(!quit.equalsIgnoreCase("quit"));
		try 
		{
			long timetaken_milli = insta_end.toEpochMilli() - insta_start.toEpochMilli(); //This will return the time in milliseconds. 
			long timetaken_sec = insta_end.getEpochSecond() - insta_start.getEpochSecond(); // This is the same as above, but it's working in seconds
			logpt.append(lnsep + "The connection has been severed by the client at time: " + form.format(dnt));
			logpt.append(lnsep + "The connection lasted : " + timetaken_sec+" sec" + " (More precisely: " + timetaken_milli + " milliseconds)");
			logpt.flush();
			logpt.append(lnsep + "-----------------------------------------------------------------------");
			logpt.flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public static void main(String args[]) 
	{
		Server srv = new Server(42069);
		srv.listen();
		srv.quit_srv();
		srv.close();
	}

	
	
}
