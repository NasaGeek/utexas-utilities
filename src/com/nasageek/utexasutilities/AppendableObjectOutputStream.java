package com.nasageek.utexasutilities;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

//ObjectOutputStream with Serializable does not work with File append because of headers. this little trick 
//found: http://stackoverflow.com/questions/1194656/appending-to-an-objectoutputstream, 
//fixes that problem

public class AppendableObjectOutputStream extends ObjectOutputStream {

	  public AppendableObjectOutputStream(OutputStream out) throws IOException {
	    super(out);
	  }

	  @Override
	  protected void writeStreamHeader() throws IOException {
	    // do not write a header
	  }

	}