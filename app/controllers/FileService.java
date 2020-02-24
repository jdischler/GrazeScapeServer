package controllers;

import play.mvc.*;
import java.io.*;

//--------------------------------------------------------------------------
public class FileService extends Controller {

       static String path = "./public/dynamicFiles/";
       
       public Result getFile(String file){
              File myfile = new File (path+file);
              return ok(myfile);
       }
}
