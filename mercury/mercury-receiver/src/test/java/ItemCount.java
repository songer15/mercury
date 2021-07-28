import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ItemCount {
    private static int lineCount;//表示代码行数
    private static int fileCount;//表示文件个数
    private static int count;//表示总行数

    public int getLineCount() {
        return lineCount;
    }

    public int getFileCount() {
        return fileCount;
    }

//    public static void main(String[] args) throws IOException {
//        ItemCount itemCount = new ItemCount();
//        // path是项目的绝对路径
//        String pathJava = "C:\\Users\\Administrator\\IdeaProjects\\devops-metric\\vms-metric\\metric-spider\\src";
//       // String pathXml = "G:/android/second/MCNBlog/res";
//        itemCount.getJAVALineNum(new File(pathJava));//将绝对路径下的文件对象作为参数传递给JAVALineNum方法
//        System.out.println("该项目一共有" + itemCount.getFileCount() + "个JAVA源文件,"
//                + itemCount.getLineCount() + "行代码");
//        count = lineCount;
//        lineCount = 0;
//        fileCount = 0;
//        //itemCount.getXMLLineNum(new File(pathXml));
//        //System.out.println("该项目一共有" + itemCount.getFileCount() + "个XML源文件,"
//        //        + itemCount.getLineCount() + "行代码");
//
//        System.out.println("该项目总共有：" + (lineCount + count) + "行代码");
//
//    }

    // 递归
    public void getJAVALineNum(File path) throws IOException {
        if (path.isFile() && path.getName().endsWith(".java")) {//如果该路径下的是文件，并且文件的后缀为.java 就将文件通过流读入缓冲区
            BufferedReader br = new BufferedReader(new FileReader(path));
            fileCount++;//java文件的个数进行累加
            while (br.readLine() != null) {//如果缓冲区中得字符流不为空，就依次向下一行读。
                lineCount++;
            }
            // System.out.println(path.getName());
            br.close();//关闭缓冲区流
        } else if (path.isDirectory()) {
            File[] listFiles = path.listFiles();
            for (File file : listFiles) {
                getJAVALineNum(file);
            }
        }
    }

    // 递归
    public void getXMLLineNum(File path) throws IOException {
        if (path.isFile() && path.getName().endsWith(".xml")) {
            BufferedReader br = new BufferedReader(new FileReader(path));
            fileCount++;
            while (br.readLine() != null) {
                lineCount++;
            }
            // System.out.println(path.getName());
            br.close();
        } else if (path.isDirectory()) {
            File[] listFiles = path.listFiles();
            for (File file : listFiles) {
                getXMLLineNum(file);
            }
        }
    }


}