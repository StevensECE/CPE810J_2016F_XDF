/**
 * @author: Lei Tang
 * 
 * add Menu to realize basic functions CREATE, OPEN, SAVE, EXIT. 
 * add Button to realize the basic functions above. 
 * In addition, add undo, cut, paste, searchButton,replaceButton if time permits.
 *
 */



package edu.stevens;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.*;
 import java.util.*;
 import java.io.*;
 import javax.swing.undo.*;
 import javax.swing.border.*;
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.event.*;
 import java.awt.datatransfer.*;

//define the external features of XDF(window/frame/UI)
public class XFrame extends JFrame implements ActionListener,DocumentListener
{	//define menu bar
	JMenu fileMenu,editMenu,formatMenu,viewMenu,helpMenu;
	//Right click item 
	JPopupMenu popupMenu;
	JMenuItem popupMenu_Undo,popupMenu_Cut,popupMenu_Copy,popupMenu_Paste,popupMenu_Delete,popupMenu_SelectAll;
	//items of FILE
	JMenuItem fileMenu_New,fileMenu_Open,fileMenu_Save,fileMenu_SaveAs,fileMenu_PageSetUp,fileMenu_Print,fileMenu_Exit;
	//items of EDIT
	JMenuItem editMenu_Undo,editMenu_Cut,editMenu_Copy,editMenu_Paste,editMenu_Delete,editMenu_Find,editMenu_FindNext,editMenu_Replace,editMenu_GoTo,editMenu_SelectAll,editMenu_TimeDate;
	//items of FORMAT
	JCheckBoxMenuItem formatMenu_LineWrap;
	JMenuItem formatMenu_Font;
	//item of VIEW
	JCheckBoxMenuItem viewMenu_Status;
	//item of HELP
	JMenuItem helpMenu_HelpTopics,helpMenu_AboutXFrame;
	//text area
	JTextArea editArea;
	//status label
	JLabel statusLabel;
	//clipboard
	Toolkit toolkit=Toolkit.getDefaultToolkit();
	Clipboard clipBoard=toolkit.getSystemClipboard();
	//Create undo manager 
	protected UndoManager undo=new UndoManager();
	protected UndoableEditListener undoHandler=new UndoHandler();
	//other things
	String oldValue;//store the original content of the edit area for comparing text changes 
	boolean isNewFile=true;//Whether the new file (not saved) 
	File currentFile;//Current file name 
	//Constructor start 
	public XFrame()
	{	
		super("eXtreme Document Format");
		//Change system default font 
		Font font = new Font("Dialog", Font.PLAIN, 14);
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, font);
			}
		}
		//Create menu bar 
		JMenuBar menuBar=new JMenuBar();
		//Create the file menu and menu items and register the event listener
		fileMenu=new JMenu("FILE");
		fileMenu.setMnemonic('F');//shortcut keys 

		fileMenu_New=new JMenuItem("NEW");
		fileMenu_New.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
		fileMenu_New.addActionListener(this);

		fileMenu_Open=new JMenuItem("OPEN");
		fileMenu_Open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_MASK));
		fileMenu_Open.addActionListener(this);

		fileMenu_Save=new JMenuItem("SAVE");
		fileMenu_Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
		fileMenu_Save.addActionListener(this);

		fileMenu_SaveAs=new JMenuItem("SAVE AS");
		fileMenu_SaveAs.addActionListener(this);

		fileMenu_PageSetUp=new JMenuItem("PAGE SET");
		fileMenu_PageSetUp.addActionListener(this);

		fileMenu_Print=new JMenuItem("PRINT");
		fileMenu_Print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK)); 
		fileMenu_Print.addActionListener(this);

		fileMenu_Exit=new JMenuItem("EXIT");
		fileMenu_Exit.addActionListener(this);

		//Create Edit menu and menu item and register the event listener 
		editMenu=new JMenu("EDIT");
		editMenu.setMnemonic('E');//shortcut key
		//When selecting the edit menu, set the availability of cut, copy, paste, delete and other functions 
		editMenu.addMenuListener(new MenuListener()
		{	public void menuCanceled(MenuEvent e)//Call to cancel the menu 
			{	checkMenuItemEnabled();//Set the availability of cut, copy, paste, delete and other functions 
			}
			public void menuDeselected(MenuEvent e)//Call to cancel the selection of a menu
			{	checkMenuItemEnabled();//Set the availability of cut, copy, paste, delete and other functions 
			}
			public void menuSelected(MenuEvent e)//Call when selecting a menu 
			{	checkMenuItemEnabled();//Set the availability of cut, copy, paste, delete and other functions 
			}
		});

		editMenu_Undo=new JMenuItem("UNDO");
		editMenu_Undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_MASK));
		editMenu_Undo.addActionListener(this);
		editMenu_Undo.setEnabled(false);

		editMenu_Cut=new JMenuItem("CUT");
		editMenu_Cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.CTRL_MASK));
		editMenu_Cut.addActionListener(this);

		editMenu_Copy=new JMenuItem("COPY");
		editMenu_Copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK));
		editMenu_Copy.addActionListener(this);

		editMenu_Paste=new JMenuItem("PASTE");
		editMenu_Paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.CTRL_MASK));
		editMenu_Paste.addActionListener(this);

		editMenu_Delete=new JMenuItem("DELETE");
		editMenu_Delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		editMenu_Delete.addActionListener(this);

		editMenu_Find=new JMenuItem("FIND");
		editMenu_Find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));
		editMenu_Find.addActionListener(this);

		editMenu_FindNext=new JMenuItem("FIND NEXT");
		editMenu_FindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		editMenu_FindNext.addActionListener(this);

		editMenu_Replace = new JMenuItem("REPLACE",'R'); 
		editMenu_Replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK)); 
		editMenu_Replace.addActionListener(this);

		editMenu_GoTo = new JMenuItem("GOTO",'G'); 
		editMenu_GoTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK)); 
		editMenu_GoTo.addActionListener(this);

		editMenu_SelectAll = new JMenuItem("SELECT ALL",'A'); 
		editMenu_SelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)); 
		editMenu_SelectAll.addActionListener(this);

		editMenu_TimeDate = new JMenuItem("TIME/DATE",'D');
		editMenu_TimeDate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		editMenu_TimeDate.addActionListener(this);

		//Create the format menu and menu item and register the event listener
		formatMenu=new JMenu("FORMAT");
		formatMenu.setMnemonic('O');//shortcut key ALT+O

		formatMenu_LineWrap=new JCheckBoxMenuItem("LINE WRAP");
		formatMenu_LineWrap.setMnemonic('W');//shortcut key ALT+W
		formatMenu_LineWrap.setState(true);
		formatMenu_LineWrap.addActionListener(this);

		formatMenu_Font=new JMenuItem("FONT");
		formatMenu_Font.addActionListener(this);

		//Create the view menu and menu item and register the event listener
		viewMenu=new JMenu("VIEW");
		viewMenu.setMnemonic('V');//shortcut key ALT+V

		viewMenu_Status=new JCheckBoxMenuItem("STATUS");
		viewMenu_Status.setMnemonic('S');//shortcut key ALT+S
		viewMenu_Status.setState(true);
		viewMenu_Status.addActionListener(this);

		//Create a help menu and menu item and register the event listener 
		helpMenu = new JMenu("HELP");
		helpMenu.setMnemonic('H');//shortcut key ALT+H

		helpMenu_HelpTopics = new JMenuItem("HELP DETAILS"); 
		helpMenu_HelpTopics.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		helpMenu_HelpTopics.addActionListener(this);

		helpMenu_AboutXFrame = new JMenuItem("ABOUT"); 
		helpMenu_AboutXFrame.addActionListener(this);

		//Add the "file" menu and menu item to the menu bar 
		menuBar.add(fileMenu); 
		fileMenu.add(fileMenu_New); 
		fileMenu.add(fileMenu_Open); 
		fileMenu.add(fileMenu_Save); 
		fileMenu.add(fileMenu_SaveAs); 
		fileMenu.addSeparator();		//gap line
		fileMenu.add(fileMenu_PageSetUp); 
		fileMenu.add(fileMenu_Print); 
		fileMenu.addSeparator();		//gap line
		fileMenu.add(fileMenu_Exit); 

		//Add the "edit" menu and menu item to the menu bar
		menuBar.add(editMenu); 
		editMenu.add(editMenu_Undo);  
		editMenu.addSeparator();		//gap line
		editMenu.add(editMenu_Cut); 
		editMenu.add(editMenu_Copy); 
		editMenu.add(editMenu_Paste); 
		editMenu.add(editMenu_Delete); 
		editMenu.addSeparator(); 		//gap line
		editMenu.add(editMenu_Find); 
		editMenu.add(editMenu_FindNext); 
		editMenu.add(editMenu_Replace);
		editMenu.add(editMenu_GoTo); 
		editMenu.addSeparator();  		//gap line
		editMenu.add(editMenu_SelectAll); 
		editMenu.add(editMenu_TimeDate);

		//Add the "format" menu and menu item to the menu bar		
		menuBar.add(formatMenu); 
		formatMenu.add(formatMenu_LineWrap); 
		formatMenu.add(formatMenu_Font);

		//Add the "view" menu and menu item to the menu bar
		menuBar.add(viewMenu); 
		viewMenu.add(viewMenu_Status);

		//Add the "help" menu and menu item to the menu bar
		menuBar.add(helpMenu);
		helpMenu.add(helpMenu_HelpTopics);
		helpMenu.addSeparator();
		helpMenu.add(helpMenu_AboutXFrame);
				
		//Add menu bar to window 				
		this.setJMenuBar(menuBar);

		//Create a text edit area and add a scroll bar
		editArea=new JTextArea(20,50);
		JScrollPane scroller=new JScrollPane(editArea);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(scroller,BorderLayout.CENTER);//
		editArea.setWrapStyleWord(true);//set linewrap
		editArea.setLineWrap(true);//true for wrap
		oldValue=editArea.getText();//get the contents of the original text editing area

		//Edit area registered event listener (related to undo operation) 
		editArea.getDocument().addUndoableEditListener(undoHandler);
		editArea.getDocument().addDocumentListener(this);

		//Right click to create a pop-up menu 
		popupMenu=new JPopupMenu();
		popupMenu_Undo=new JMenuItem("UNDO");
		popupMenu_Cut=new JMenuItem("CUT");
		popupMenu_Copy=new JMenuItem("COPY");
		popupMenu_Paste=new JMenuItem("PASTE");
		popupMenu_Delete=new JMenuItem("DELETE");
		popupMenu_SelectAll=new JMenuItem("SELECTALL");

		popupMenu_Undo.setEnabled(false);

		//Add menu item and separator to the right click menu. 
		popupMenu.add(popupMenu_Undo);
		popupMenu.addSeparator();
		popupMenu.add(popupMenu_Cut);
		popupMenu.add(popupMenu_Copy);
		popupMenu.add(popupMenu_Paste);
		popupMenu.add(popupMenu_Delete);
		popupMenu.addSeparator();
		popupMenu.add(popupMenu_SelectAll);

		//Text edit area register event 
		popupMenu_Undo.addActionListener(this);
		popupMenu_Cut.addActionListener(this);
		popupMenu_Copy.addActionListener(this);
		popupMenu_Paste.addActionListener(this);
		popupMenu_Delete.addActionListener(this);
		popupMenu_SelectAll.addActionListener(this);

		//Text edit area register event 
		editArea.addMouseListener(new MouseAdapter()
		{	public void mousePressed(MouseEvent e)
			{	if(e.isPopupTrigger())//Returns whether the mouse event is a trigger event for the platform's pop-up menu
				{	popupMenu.show(e.getComponent(),e.getX(),e.getY());//post the menu at the calling location 
				}
				checkMenuItemEnabled();//Set the availability of cut, copy, paste, delete and other functions 
				editArea.requestFocus();//Edit area get focus 
			}
			public void mouseReleased(MouseEvent e)
			{	if(e.isPopupTrigger())//Returns whether the mouse event is a trigger event for the platform's pop-up menu
				{	popupMenu.show(e.getComponent(),e.getX(),e.getY());//post the menu at the calling location 
				}
				checkMenuItemEnabled();//Set the availability of cut, copy, paste, delete and other functions 
				editArea.requestFocus();//Edit area get focus 
			}
		});//Text editing area registered&& right click menu event /end 

		//Create and add status bar 
		statusLabel=new JLabel("　Get help information pressing F1!");
		this.add(statusLabel,BorderLayout.SOUTH);//Add status bar tab to window 

		//Set the location, size, and visibility of the window on the screen. 
		this.setLocation(200,50);
		this.setSize(1050,650);
		this.setVisible(true);
		//Add the listener window 
		addWindowListener(new WindowAdapter()
		{	public void windowClosing(WindowEvent e)
			{	exitWindowChoose();
			}
		});

		checkMenuItemEnabled();
		editArea.requestFocus();
	}//end of constructor
	
	//Set menu item availability: cut, copy, paste, delete function 
	public void checkMenuItemEnabled()
	{
		String selectText=editArea.getSelectedText();
		if(selectText==null)
		{	editMenu_Cut.setEnabled(false);
			popupMenu_Cut.setEnabled(false);
			editMenu_Copy.setEnabled(false);
			popupMenu_Copy.setEnabled(false);
			editMenu_Delete.setEnabled(false);
			popupMenu_Delete.setEnabled(false);
		}
		else
		{	editMenu_Cut.setEnabled(true);
			popupMenu_Cut.setEnabled(true); 
			editMenu_Copy.setEnabled(true);
			popupMenu_Copy.setEnabled(true);
			editMenu_Delete.setEnabled(true);
			popupMenu_Delete.setEnabled(true);
		}
		//paste function availability judgment 
		Transferable contents=clipBoard.getContents(this);
		if(contents==null)
		{	editMenu_Paste.setEnabled(false);
			popupMenu_Paste.setEnabled(false);
		}
		else
		{	editMenu_Paste.setEnabled(true);
			popupMenu_Paste.setEnabled(true);	
		}
	}

	//Call when closing the window
	public void exitWindowChoose()
	{
		editArea.requestFocus();
		String currentValue=editArea.getText();
		if(currentValue.equals(oldValue)==true)
		{	System.exit(0);
		}
		else
		{	int exitChoose=JOptionPane.showConfirmDialog(this,"Your file has not been saved. Do you want to save it? ","Exit Alert",JOptionPane.YES_NO_CANCEL_OPTION);
			if(exitChoose==JOptionPane.YES_OPTION)
			{	//boolean isSave=false;
				if(isNewFile)
				{	
					String str=null;
					JFileChooser fileChooser=new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setApproveButtonText("Confirm");
					fileChooser.setDialogTitle("Save As");
					
					int result=fileChooser.showSaveDialog(this);
					
					if(result==JFileChooser.CANCEL_OPTION)
					{	statusLabel.setText("You did not save the file!");
						return;
					}					
	
					File saveFileName=fileChooser.getSelectedFile();
				
					if(saveFileName==null||saveFileName.getName().equals(""))
					{	JOptionPane.showMessageDialog(this,"Illegal file name!","Illegal file name!",JOptionPane.ERROR_MESSAGE);
					}
					else 
					{	try
						{	FileWriter fw=new FileWriter(saveFileName);
							BufferedWriter bfw=new BufferedWriter(fw);
							bfw.write(editArea.getText(),0,editArea.getText().length());
							bfw.flush();
							fw.close();
							
							isNewFile=false;
							currentFile=saveFileName;
							oldValue=editArea.getText();
							
							this.setTitle(saveFileName.getName()+"  - File");
							statusLabel.setText("Open current file"+saveFileName.getAbsoluteFile());
							//isSave=true;
						}							
						catch(IOException ioException){					
						}				
					}
				}
				else
				{
					try
					{	FileWriter fw=new FileWriter(currentFile);
						BufferedWriter bfw=new BufferedWriter(fw);
						bfw.write(editArea.getText(),0,editArea.getText().length());
						bfw.flush();
						fw.close();
						//isSave=true;
					}							
					catch(IOException ioException){					
					}
				}
				System.exit(0);
				//if(isSave)System.exit(0);
				//else return;
			}
			else if(exitChoose==JOptionPane.NO_OPTION)
			{	System.exit(0);
			}
			else
			{	return;
			}
		}
	}

	//method of find word
	public void find(){}
	
	//method of replace word
	public void replace(){}

	//font method
	public void font(){}
	
	// The action of each button 
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==fileMenu_New)
		{	editArea.requestFocus();
			String currentValue=editArea.getText();
			boolean isTextChange=(currentValue.equals(oldValue))?false:true;
			if(isTextChange)
			{	int saveChoose=JOptionPane.showConfirmDialog(this,"Your file has not been saved. Do you want to save it? ","hint",JOptionPane.YES_NO_CANCEL_OPTION);
				if(saveChoose==JOptionPane.YES_OPTION)
				{	String str=null;
					JFileChooser fileChooser=new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
					fileChooser.setDialogTitle("save as");
					int result=fileChooser.showSaveDialog(this);
					if(result==JFileChooser.CANCEL_OPTION)
					{	statusLabel.setText("select no file");
						return;
					}
					File saveFileName=fileChooser.getSelectedFile();
					if(saveFileName==null || saveFileName.getName().equals(""))
					{	JOptionPane.showMessageDialog(this,"Illegal file name","Illegal file name",JOptionPane.ERROR_MESSAGE);
					}
					else 
					{	try
						{	FileWriter fw=new FileWriter(saveFileName);
							BufferedWriter bfw=new BufferedWriter(fw);
							bfw.write(editArea.getText(),0,editArea.getText().length());
							bfw.flush();//flushing the buffer
							bfw.close();
							isNewFile=false;
							currentFile=saveFileName;
							oldValue=editArea.getText();
							this.setTitle(saveFileName.getName()+" - XDF");
							statusLabel.setText("Current opened file："+saveFileName.getAbsoluteFile());
						}
						catch (IOException ioException)
						{
						}
					}
				}
				else if(saveChoose==JOptionPane.NO_OPTION)
				{	editArea.replaceRange("",0,editArea.getText().length());
					statusLabel.setText(" create new file");
					this.setTitle("no title - XDF");
					isNewFile=true;
					undo.discardAllEdits();	//discard all Undo operate
					editMenu_Undo.setEnabled(false);
					oldValue=editArea.getText();
				}
				else if(saveChoose==JOptionPane.CANCEL_OPTION)
				{	return;
				}
			}
			else
			{	editArea.replaceRange("",0,editArea.getText().length());
				statusLabel.setText(" create new file");
				this.setTitle("no title - XDF");
				isNewFile=true;
				undo.discardAllEdits();//discard all Undo operate
				editMenu_Undo.setEnabled(false);
				oldValue=editArea.getText();
			}
		}//finish new function
		//Open function
		else if(e.getSource()==fileMenu_Open)
		{	editArea.requestFocus();
			String currentValue=editArea.getText();
			boolean isTextChange=(currentValue.equals(oldValue))?false:true;
			if(isTextChange)
			{	int saveChoose=JOptionPane.showConfirmDialog(this,"Your file has not been saved. Do you want to save it? ","hint",JOptionPane.YES_NO_CANCEL_OPTION);
				if(saveChoose==JOptionPane.YES_OPTION)
				{	String str=null;
					JFileChooser fileChooser=new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					
					fileChooser.setDialogTitle("save as");
					int result=fileChooser.showSaveDialog(this);
					if(result==JFileChooser.CANCEL_OPTION)
					{	statusLabel.setText("select no file");
						return;
					}
					File saveFileName=fileChooser.getSelectedFile();
					if(saveFileName==null || saveFileName.getName().equals(""))
					{	JOptionPane.showMessageDialog(this,"Illegal file name","Illegal file name",JOptionPane.ERROR_MESSAGE);
					}
					else 
					{	try
						{	FileWriter fw=new FileWriter(saveFileName);
							BufferedWriter bfw=new BufferedWriter(fw);
							bfw.write(editArea.getText(),0,editArea.getText().length());
							bfw.flush();//flushing the buffer
							bfw.close();
							isNewFile=false;
							currentFile=saveFileName;
							oldValue=editArea.getText();
							this.setTitle(saveFileName.getName()+" - XDF");
							statusLabel.setText("Current opened file："+saveFileName.getAbsoluteFile());
						}
						catch (IOException ioException)
						{
						}
					}
				}
				else if(saveChoose==JOptionPane.NO_OPTION)
				{	String str=null;
					JFileChooser fileChooser=new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					
					fileChooser.setDialogTitle("Open file");
					int result=fileChooser.showOpenDialog(this);
					if(result==JFileChooser.CANCEL_OPTION)
					{	statusLabel.setText("select no file");
						return;
					}
					File fileName=fileChooser.getSelectedFile();
					if(fileName==null || fileName.getName().equals(""))
					{	JOptionPane.showMessageDialog(this,"Illegal file name","Illegal file name",JOptionPane.ERROR_MESSAGE);
					}
					else
					{	try
						{	FileReader fr=new FileReader(fileName);
							BufferedReader bfr=new BufferedReader(fr);
							editArea.setText("");
							while((str=bfr.readLine())!=null)
							{	editArea.append(str);
							}
							this.setTitle(fileName.getName()+" - XDF");
							statusLabel.setText(" Current opened file："+fileName.getAbsoluteFile());
							fr.close();
							isNewFile=false;
							currentFile=fileName;
							oldValue=editArea.getText();
						}
						catch (IOException ioException)
						{
						}
					}
				}
				else
				{	return;
				}
			}
			else
			{	String str=null;
				JFileChooser fileChooser=new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				fileChooser.setDialogTitle("Open file");
				int result=fileChooser.showOpenDialog(this);
				if(result==JFileChooser.CANCEL_OPTION)
				{	statusLabel.setText(" select no file ");
					return;
				}
				File fileName=fileChooser.getSelectedFile();
				if(fileName==null || fileName.getName().equals(""))
				{	JOptionPane.showMessageDialog(this,"Illegal file name","Illegal file name",JOptionPane.ERROR_MESSAGE);
				}
				else
				{	try
					{	FileReader fr=new FileReader(fileName);
						BufferedReader bfr=new BufferedReader(fr);
						editArea.setText("");
						while((str=bfr.readLine())!=null)
						{	editArea.append(str);
						}
						this.setTitle(fileName.getName()+" - XDF");
						statusLabel.setText(" Current opened file："+fileName.getAbsoluteFile());
						fr.close();
						isNewFile=false;
						currentFile=fileName;
						oldValue=editArea.getText();
					}
					catch (IOException ioException)
					{
					}
				}
			}
		}//finish open function
		//save funtion begin
		else if(e.getSource()==fileMenu_Save)
		{	editArea.requestFocus();
			if(isNewFile)
			{	String str=null;
				JFileChooser fileChooser=new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				fileChooser.setDialogTitle("save");
				int result=fileChooser.showSaveDialog(this);
				if(result==JFileChooser.CANCEL_OPTION)
				{	statusLabel.setText("select no file");
					return;
				}
				File saveFileName=fileChooser.getSelectedFile();
				if(saveFileName==null || saveFileName.getName().equals(""))
				{	JOptionPane.showMessageDialog(this,"Illegal file name","Illegal file name",JOptionPane.ERROR_MESSAGE);
				}
				else 
				{	try
					{	FileWriter fw=new FileWriter(saveFileName);
						BufferedWriter bfw=new BufferedWriter(fw);
						bfw.write(editArea.getText(),0,editArea.getText().length());
						bfw.flush();//flushing the buffer
						bfw.close();
						isNewFile=false;
						currentFile=saveFileName;
						oldValue=editArea.getText();
						this.setTitle(saveFileName.getName()+" - XDF");
						statusLabel.setText("Current opened file："+saveFileName.getAbsoluteFile());
					}
					catch (IOException ioException)
					{
					}
				}
			}
			else
			{	try
				{	FileWriter fw=new FileWriter(currentFile);
					BufferedWriter bfw=new BufferedWriter(fw);
					bfw.write(editArea.getText(),0,editArea.getText().length());
					bfw.flush();
					fw.close();
				}							
				catch(IOException ioException)
				{					
				}
			}
		}//finish the save function
		
		//save as function begin
		else if(e.getSource()==fileMenu_SaveAs)
		{	editArea.requestFocus();
			String str=null;
			JFileChooser fileChooser=new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			fileChooser.setDialogTitle("save as");
			int result=fileChooser.showSaveDialog(this);
			if(result==JFileChooser.CANCEL_OPTION)
			{	statusLabel.setText("　select no file");
				return;
			}				
			File saveFileName=fileChooser.getSelectedFile();
			if(saveFileName==null||saveFileName.getName().equals(""))
			{	JOptionPane.showMessageDialog(this,"Illegal file name","Illegal file name",JOptionPane.ERROR_MESSAGE);
			}	
			else 
			{	try
				{	FileWriter fw=new FileWriter(saveFileName);
					BufferedWriter bfw=new BufferedWriter(fw);
					bfw.write(editArea.getText(),0,editArea.getText().length());
					bfw.flush();
					fw.close();
					oldValue=editArea.getText();
					this.setTitle(saveFileName.getName()+"  - XDF");
					statusLabel.setText("　Current opened file:"+saveFileName.getAbsoluteFile());
				}						
				catch(IOException ioException)
				{					
				}				
			}
		}//finish save as function
		//page setup
				else if(e.getSource()==fileMenu_PageSetUp)
				{	editArea.requestFocus();
					JOptionPane.showMessageDialog(this,"Sorry, this feature is not yet implemented! ","hint",JOptionPane.WARNING_MESSAGE);
				}//finish page setup
				
				//print function begin
				else if(e.getSource()==fileMenu_Print)
				{	editArea.requestFocus();
					JOptionPane.showMessageDialog(this,"Sorry, this feature is not yet implemented! ","hint",JOptionPane.WARNING_MESSAGE);
				}//finish print function
				
				//exit function begin
				else if(e.getSource()==fileMenu_Exit)
				{	int exitChoose=JOptionPane.showConfirmDialog(this,"Are you exit? ( shuts down )","exit alert",JOptionPane.OK_CANCEL_OPTION);
					if(exitChoose==JOptionPane.OK_OPTION)
					{	System.exit(0);
					}
					else
					{	return;
					}
				}//finish exit function
				
				//undo function begin
				else if(e.getSource()==editMenu_Undo || e.getSource()==popupMenu_Undo)
				{	editArea.requestFocus();
					if(undo.canUndo())
					{	try
						{	undo.undo();
						}
						catch (CannotUndoException ex)
						{	System.out.println("Unable to undo:" + ex);
							//ex.printStackTrace();
						}
					}
					if(!undo.canUndo())
						{	editMenu_Undo.setEnabled(false);
						}
				}//finish undo function
				
				//cut function
				else if(e.getSource()==editMenu_Cut || e.getSource()==popupMenu_Cut)
				{	editArea.requestFocus();
					String text=editArea.getSelectedText();
					StringSelection selection=new StringSelection(text);
					clipBoard.setContents(selection,null);
					editArea.replaceRange("",editArea.getSelectionStart(),editArea.getSelectionEnd());
					checkMenuItemEnabled();//Set the availability of cut, copy, paste, and delete functions. 
				}//finish cut function
				
		//copy function begin
				else if(e.getSource()==editMenu_Copy || e.getSource()==popupMenu_Copy)
				{	editArea.requestFocus();
					String text=editArea.getSelectedText();
					StringSelection selection=new StringSelection(text);
					clipBoard.setContents(selection,null);
					checkMenuItemEnabled();//Set the availability of cut, copy, paste, and delete functions. 
				}//finish copy function
		
				//paste function begin
				else if(e.getSource()==editMenu_Paste || e.getSource()==popupMenu_Paste)
				{	editArea.requestFocus();
					Transferable contents=clipBoard.getContents(this);
					if(contents==null)return;
					String text="";
					try
					{	text=(String)contents.getTransferData(DataFlavor.stringFlavor);
					}
					catch (Exception exception)
					{
					}
					editArea.replaceRange(text,editArea.getSelectionStart(),editArea.getSelectionEnd());
					checkMenuItemEnabled();
				}//finish paste function
		
				//delete function begin 
				else if(e.getSource()==editMenu_Delete || e.getSource()==popupMenu_Delete)
				{	editArea.requestFocus();
					editArea.replaceRange("",editArea.getSelectionStart(),editArea.getSelectionEnd());
					checkMenuItemEnabled();	//Set the availability of cut, copy, paste, and delete functions. 
				}//finish delete function
		
				//find function
				else if(e.getSource()==editMenu_Find)
				{	editArea.requestFocus();
					find();
				}//finish find function
		
				//findnext function
				else if(e.getSource()==editMenu_FindNext)
				{	editArea.requestFocus();
					find();
				}//finish findnext function
		
				//replace function
				else if(e.getSource()==editMenu_Replace)
				{	editArea.requestFocus();
					replace();
				}//finish replace function
		
				//goto function
				else if(e.getSource()==editMenu_GoTo)
				{	editArea.requestFocus();
					JOptionPane.showMessageDialog(this,"Sorry, this feature is not yet implemented! ","hint",JOptionPane.WARNING_MESSAGE);
				}//not implemented
		
				//time and date
				else if(e.getSource()==editMenu_TimeDate)
				{	editArea.requestFocus();
					//SimpleDateFormat currentDateTime=new SimpleDateFormat("HH:mmyyyy-MM-dd");
					//editArea.insert(currentDateTime.format(new Date()),editArea.getCaretPosition());
					Calendar rightNow=Calendar.getInstance();
					Date date=rightNow.getTime();
					editArea.insert(date.toString(),editArea.getCaretPosition());
				}//finish timedate
		
				//select all function
				else if(e.getSource()==editMenu_SelectAll || e.getSource()==popupMenu_SelectAll)
				{	editArea.selectAll();
				}//finish select all function
		
				//word Wrap (have set)
				else if(e.getSource()==formatMenu_LineWrap)
				{	if(formatMenu_LineWrap.getState())
						editArea.setLineWrap(true);
					else 
						editArea.setLineWrap(false);

				}
				//font set
				else if(e.getSource()==formatMenu_Font)
				{	editArea.requestFocus();
					font();
				}//finish font set
		
				//Set status bar visibility 
				else if(e.getSource()==viewMenu_Status)
				{	if(viewMenu_Status.getState())
						statusLabel.setVisible(true);
					else 
						statusLabel.setVisible(false);
				}//Set status bar visibility 
				
				//help menu
				else if(e.getSource()==helpMenu_HelpTopics)
				{	editArea.requestFocus();
					JOptionPane.showMessageDialog(this,"If you like something, say something!","help menu",JOptionPane.INFORMATION_MESSAGE);
				}
				//about
				else if(e.getSource()==helpMenu_AboutXFrame)
				{	editArea.requestFocus();
					JOptionPane.showMessageDialog(this,
						"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n"+
						" Author: XFrame team "+
						" Version: 1.0                          \n"+
						" Release time: 2016 fall                            \n"+
						" Description: basic functions and framework                \n"+
						" Development cycle : one month \n"+
						"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n",
						"XDF",JOptionPane.INFORMATION_MESSAGE);
				}
		

	}

	//Method to implement the "documentlistener" interface (related to undo operation) 
	public void removeUpdate(DocumentEvent e){}
	public void insertUpdate(DocumentEvent e){}
	public void changedUpdate(DocumentEvent e){}

	//Implementation of the interface undoableeditlistener class undohandler (related to undo operation)
	class UndoHandler implements UndoableEditListener
	{	public void undoableEditHappened(UndoableEditEvent uee){}
	}

}
