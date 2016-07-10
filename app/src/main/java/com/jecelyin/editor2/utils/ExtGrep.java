package com.jecelyin.editor2.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;

import com.jecelyin.common.app.JecApp;
import com.jecelyin.common.utils.IOUtils;
import com.jecelyin.common.utils.L;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor2.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author https://github.com/drippel/JavaGrep
 */
public class ExtGrep implements Parcelable {

    final List<String> includeFilePatterns = new ArrayList<String>();
    final List<String> excludeDirPatterns = new ArrayList<String>();
    boolean invertMatch = false;
    boolean ignoreCase = false;
    int maxCount = 0;
    boolean printFileNameOnly = false;
    boolean printByteOffset = false;
    boolean quiet = false;
    boolean printCountOnly = false;
    boolean printFilesWithoutMatch = false;
    boolean wordRegex = false;
    boolean lineRegex = false;
    boolean noMessages = false;
    boolean printFileName = false;
    boolean printMatchOnly = false;
    boolean printLineNumber = false;
    boolean recurseDirectories = false;
    boolean skipDirectories = false;
    List<String> excludeFilePatterns = new ArrayList<String>();
    boolean useInclude = false;
    boolean useExclude = false;
    int beforeContext = 0;
    int afterContext = 0;
    private Pattern grepPattern;
    private String regex;
    private List<File> filesToProcess = new ArrayList<>();

    public static interface OnSearchFinishListener {
        public void onFinish(List<Result> results);
    }

    public enum GrepDirect {
        PREV,
        NEXT,
    }

    public ExtGrep() {
    }

    private void printMessage(final String msg) {

//        if (!quiet) {
//            System.out.println(msg);
//        }
    }

    private List<Result> grepFiles() {
        ArrayList<Result> results = new ArrayList<Result>();
        // at this point the list was expanded into file names
        for (final File f : filesToProcess) {
            results.addAll(grepFile(f));
        }
        return results;
    }

    void readExcludeFrom(final String vals[]) {

        for (String val : vals) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(val)));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    excludeFilePatterns.add(line);
                }

            } catch (IOException e) {
                L.e(e);
            }
        }

    }

    void readIncludesFrom(final String vals[]) {

        for (String val : vals) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(val)));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    includeFilePatterns.add(line);
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void printErrorMessage(final String msg) {

        if (!noMessages) {
            System.out.println(msg);
        }
    }

    private Result printMatch(final File file, final String line, final int lineNumber,
                              final int startOffset, final int endOffset, final int lineStartOffset, final List<String> beforeContextLines,
                              final List<String> afterContextLines) {
        int start = lineStartOffset - 20 < 0 ? 0 : lineStartOffset - 20;
        int end = lineStartOffset + 20 >= line.length() ? line.length() : lineStartOffset + 20;
        Result result = new Result();
        result.file = file;
        result.line = line.substring((int) start, (int) end);
        result.lineNumber = lineNumber;
        result.startOffset = startOffset;
        result.endOffset = endOffset;
        result.lineStartOffset = lineStartOffset;
        return result;
//        if( quiet || printFilesWithoutMatch || printCountOnly ) {
//            return;
//        }

        /*
         * if( ( beforeContext() > 0 ) || ( afterContext() > 0 ) ) {
         * System.out.println( "--" );
         * }
         */

//        if( beforeContext > 0 ) {
//            for( String bc : beforeContextLines ) {
//                String s = new String( bc );
//                if( filesToProcess.size() > 1 ) {
//                    try {
//                        s = file.getCanonicalPath() + "-" + s;
//                    }
//                    catch( IOException ioe ) {}
//                }
//                System.out.println( s );
//            }
//        }
//
//        String msg = "";
//        if( printFileNamesOnly() ) {
//            try {
//                System.out.println( String.format( "%1$s", file.getCanonicalPath() ) );
//            }
//            catch( IOException ioe ) {
//                // TODO:
//            }
//            return;
//        }
//
//        if( printFileName ) {
//            try {
//                msg += file.getCanonicalPath() + ":";
//            }
//            catch( IOException ioe ) {}
//        }
//
//        if( printLineNumber ) {
//            msg += lineNumber + ":";
//        }
//
//        if( printByteOffset ) {
//            msg += byteOffset + ":";
//        }
//
//        msg = msg.length() > 1 ? msg + "    " + line : line;
//
//        System.out.println( msg );
//
//        if( afterContext > 0 ) {
//            for( String ac : afterContextLines ) {
//                String s = new String( ac );
//                if( filesToProcess.size() > 1 ) {
//                    try {
//                        s = file.getCanonicalPath() + "-" + s;
//                    }
//                    catch( IOException ioe ) {}
//                }
//                System.out.println( s );
//            }
//        }
//
//        if( ( beforeContext > 0 ) || ( afterContext > 0 ) ) {
//            System.out.println( "--" );
//        }
    }

    // TODO: note that file names can contain wild cards too
    // so this method should also expand directories as well
    private void processFileArgs(final List list) {

        filesToProcess = new ArrayList<File>();

        for (final Object o : list) {
            final String name = (String) o;

            final File f = new File(name);

            if (f.exists()) {
                if (f.isFile()) {
                    if (includeFile(f)) {
                        if (!excludeFile(f)) {
                            filesToProcess.add(f);
                        }
                    }
                } else if (f.isDirectory()) {
                    if (recurseDirectories) {
                        filesToProcess.addAll(recurseDir(f));
                    }
                }
            }
        }

    }

    public void verifyFileList() {

        List<File> list = filesToProcess;
        filesToProcess = new ArrayList<File>();

        for (final Object o : list) {

            final File f = (File) o;

            if (f.exists()) {
                if (f.isFile()) {
                    if (includeFile(f)) {
                        if (!excludeFile(f)) {
                            filesToProcess.add(f);
                        }
                    }
                } else if (f.isDirectory()) {
                    if (recurseDirectories) {
                        filesToProcess.addAll(recurseDir(f));
                    }
                }
            }
        }

    }

    private List<File> recurseDir(final File dir) {

        List<File> files = new ArrayList<File>();

        for (File f : dir.listFiles()) {

            if (f.isFile()) {
                if (includeFile(f)) {
                    if (!excludeFile(f)) {
                        files.add(f);
                    }
                }
            } else if (f.isDirectory()) {
                if (!excludeDir(f)) {
                    files.addAll(recurseDir(f));
                }
            }

        }

        return files;
    }

    void readRegexFromFile(final String... fnames) {

//        for( String fname : fnames ) {
//
//            File file = new File( fname );
//            if( file.exists() && file.isFile() ) {
//
//                try {
//
//                    regexes.addAll( FileUtils.readLines( file ) );
//                }
//                catch( IOException io ) {
//                    // TODO - what to do here
//                }
//
//            }
//        }


    }

    void readLongRegexFromFile(final String[] fnames) {

//        for( String fname : fnames ) {
//
//            File file = new File( fname );
//            if( file.exists() && file.isFile() ) {
//
//                try {
//                    regexes.add( FileUtils.readFileToString( file ) );
//                }
//                catch( IOException io ) {
//                    // TODO - what to do here
//                }
//
//            }
//        }

    }


//    public static Predicate wildcardMatcher( final File file ) {
//
//        return new Predicate() {
//
//            @Override
//            public boolean evaluate( final Object object ) {
//
//                String pattern = (String)object;
//                String nm = FilenameUtils.getName( file.getName() );
//                return FilenameUtils.wildcardMatch( nm, pattern );
//            }
//
//        };
//    }

    private void reset() {

        grepPattern = null;
        regex = null;
        filesToProcess = null;
        excludeFilePatterns.clear();

    }

    public boolean includeFile(final File f) {

        if (!useInclude) {
            return true;
        }

//        if( CollectionUtils.exists( includeFilePatterns, wildcardMatcher( f ) ) ) {
//            return true;
//        }

        return false;
    }

    public boolean excludeFile(final File f) {

//        if( !useExclude ) {
//            return false;
//        }
//
//        if( CollectionUtils.exists( excludeFilePatterns, wildcardMatcher( f ) ) ) {
//            return true;
//        }

        return IOUtils.isBinaryFile(f);

    }

    private boolean excludeDir(final File f) {

//        if( CollectionUtils.exists( excludeDirPatterns, wildcardMatcher( f ) ) ) {
//            return true;
//        }

        return false;
    }

    private boolean printFileNamesOnly() {

        return printFilesWithoutMatch || printFileNameOnly;
    }

    private Matcher matchAny(final CharSequence line) {
        Matcher m = grepPattern.matcher(line);
        if (m.find()) {
            return m;
        }

        return null;
    }

    public void replaceAll(Editable text, String replaceText) {
        Matcher m = grepPattern.matcher(text);
        ArrayList<Integer> array = new ArrayList<>();
        // 从头开始搜索获取所有位置
        while (m.find()) {
            array.add(m.start());
            array.add(m.end());
        }
        int size = array.size();
        if (size == 0) {
            UIUtils.toast(JecApp.getContext(), JecApp.getContext().getResources().getQuantityString(R.plurals.x_text_replaced, 0));
            return;
        }
        int count = 0;
        for (int i = size - 2; i >= 0; i -= 2) {
            count++;
            text.replace(array.get(i), array.get(i + 1), replaceText);
        }
        UIUtils.toast(JecApp.getContext(), JecApp.getContext().getResources().getQuantityString(R.plurals.x_text_replaced, count, count));
    }

    private List<Result> grepFile(final File file) {
        int lineNumber = 0;
        int count = 0;
        int byteOffset = 0;
        List<String> beforeContextLines = new LinkedList<String>();
        List<String> afterContextLines = new LinkedList<String>();
        ArrayList<Result> results = new ArrayList<>();
        BufferedReader bfr = null;
        try {
            bfr = new BufferedReader(new FileReader(file), 16000);
            for (String line = bfr.readLine(); line != null; line = bfr.readLine()) {

                lineNumber++;
                Matcher m = matchAny(line);

                if (((m != null) && !invertMatch)) {
                    count++;
                    if (afterContext > 0) {
                        afterContextLines.clear();
                        bfr.mark(4000);
                        for (int i = 0; i < afterContext; i++) {
                            String s = bfr.readLine();
                            if (s != null) {
                                afterContextLines.add(s);
                            } else {
                                break;
                            }
                        }
                        bfr.reset();
                    }
                    String match = line;
                    if (printMatchOnly) {
                        match = m.group();
                    }
//                    printMatch(file, match, lineNumber, count, (byteOffset + m.start()), beforeContextLines,
                    results.add(printMatch(file, match, lineNumber, byteOffset + m.start(), byteOffset + m.end(), m.start(), beforeContextLines,
                            afterContextLines));
                    if (printFileNameOnly) {
                        return null;
                    }
                    //
                } else if ((m == null) && invertMatch) {
                    count++;
                    results.add(printMatch(file, line, lineNumber, byteOffset, byteOffset, byteOffset, beforeContextLines,
                            afterContextLines));
                    // TODO: this has a slightly different meaning
                    if (printFileNameOnly) {
                        return null;
                    }
                }

                if ((maxCount != 0) && (count >= maxCount)) {
                    break;
                }

                byteOffset += line.length();
                byteOffset += 1; // TODO: end of line char - what about DOS/Win32?

                beforeContextLines.add(line);
                if (beforeContextLines.size() > beforeContext) {
                    beforeContextLines.remove(0);
                }
            }
        } catch (Exception ioe) {
            L.e(ioe);
        } finally {
            if (bfr != null)
                try {
                    bfr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        if (count == 0) {
            // no matches in file
            if (printFilesWithoutMatch) {
                printMessage(file.getName());
            }
        }

        if (printCountOnly) {
            printMessage((file.getName() + ":" + count));
        }

        return results;
    }

    public void grepText(final GrepDirect direct, final CharSequence line, final int start, Subscriber<int[]> subscriber) {
        Observable.create(new Observable.OnSubscribe<int[]>() {
            @Override
            public void call(Subscriber<? super int[]> subscriber) {
                try {
                    compilePattern();
                    int[] results = null;
                    int index = start;
                    Matcher m = grepPattern.matcher(line);
                    if (direct == GrepDirect.NEXT) {
                        for (int tryCount = 0; tryCount < 2; tryCount++) {
                            if (m.find(index)) {
                                results = new int[]{m.start(), m.end()};
                                break;
                            } else if (index > 0) {
                                index = 0;
                            } else {
                                break;
                            }
                        }
                    } else {
                        if (index <= 0)
                            index = line.length();

                        // 从头开始搜索获取所有位置
                        while (m.find()) {
                            if (m.end() >= index) {
                                break;
                            }
                            results = new int[]{m.start(), m.end()};
                        }
                    }

                    subscriber.onNext(results);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }

            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(subscriber);
    }

    public void setRegex(final String r) {
        regex = r;
    }

    public String getRegex() {
        return regex;
    }

    private void compilePattern() {

        int flags = Pattern.COMMENTS;

        String pattern = regex;

        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        // we are ignoring line-regex if both are supplied
        if (wordRegex) {
            // poor mans way to do it
            pattern = "\\b" + pattern + "\\b";
        } else {
            if (lineRegex) {

                // poor mans way to do it
                pattern = "^" + pattern + "$";
            }
        }

        grepPattern = Pattern.compile(pattern, flags);

    }

    public void addFile(final String name) {

        filesToProcess.add(new File(name));
    }

    public void execute(Subscriber<List<Result>> subscriber) {
        Observable.create(new Observable.OnSubscribe<List<Result>>() {
            @Override
            public void call(Subscriber<? super List<Result>> subscriber) {
                try {
                    compilePattern();
                    verifyFileList();
                    List<Result> results = grepFiles();
                    subscriber.onNext(results);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public List<File> getFilesToProcess() {
        return filesToProcess;
    }

    public static class Result {
        public File file;
        public String line;
        public int lineNumber;
        public int lineStartOffset;
        public int startOffset;
        public int endOffset;

        private Result() {
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(invertMatch ? (byte) 1 : (byte) 0);
        dest.writeByte(ignoreCase ? (byte) 1 : (byte) 0);
        dest.writeInt(this.maxCount);
        dest.writeByte(printFileNameOnly ? (byte) 1 : (byte) 0);
        dest.writeByte(printByteOffset ? (byte) 1 : (byte) 0);
        dest.writeByte(quiet ? (byte) 1 : (byte) 0);
        dest.writeByte(printCountOnly ? (byte) 1 : (byte) 0);
        dest.writeByte(printFilesWithoutMatch ? (byte) 1 : (byte) 0);
        dest.writeByte(wordRegex ? (byte) 1 : (byte) 0);
        dest.writeByte(lineRegex ? (byte) 1 : (byte) 0);
        dest.writeByte(noMessages ? (byte) 1 : (byte) 0);
        dest.writeByte(printFileName ? (byte) 1 : (byte) 0);
        dest.writeByte(printMatchOnly ? (byte) 1 : (byte) 0);
        dest.writeByte(printLineNumber ? (byte) 1 : (byte) 0);
        dest.writeByte(recurseDirectories ? (byte) 1 : (byte) 0);
        dest.writeByte(skipDirectories ? (byte) 1 : (byte) 0);
        dest.writeStringList(this.excludeFilePatterns);
        dest.writeByte(useInclude ? (byte) 1 : (byte) 0);
        dest.writeByte(useExclude ? (byte) 1 : (byte) 0);
        dest.writeInt(this.beforeContext);
        dest.writeInt(this.afterContext);
        dest.writeString(this.regex);
        dest.writeList(this.filesToProcess);
    }

    protected ExtGrep(Parcel in) {
        this.invertMatch = in.readByte() != 0;
        this.ignoreCase = in.readByte() != 0;
        this.maxCount = in.readInt();
        this.printFileNameOnly = in.readByte() != 0;
        this.printByteOffset = in.readByte() != 0;
        this.quiet = in.readByte() != 0;
        this.printCountOnly = in.readByte() != 0;
        this.printFilesWithoutMatch = in.readByte() != 0;
        this.wordRegex = in.readByte() != 0;
        this.lineRegex = in.readByte() != 0;
        this.noMessages = in.readByte() != 0;
        this.printFileName = in.readByte() != 0;
        this.printMatchOnly = in.readByte() != 0;
        this.printLineNumber = in.readByte() != 0;
        this.recurseDirectories = in.readByte() != 0;
        this.skipDirectories = in.readByte() != 0;
        this.excludeFilePatterns = in.createStringArrayList();
        this.useInclude = in.readByte() != 0;
        this.useExclude = in.readByte() != 0;
        this.beforeContext = in.readInt();
        this.afterContext = in.readInt();
        this.regex = in.readString();
        this.filesToProcess = new ArrayList<File>();
        in.readList(this.filesToProcess, List.class.getClassLoader());
    }

    public static final Creator<ExtGrep> CREATOR = new Creator<ExtGrep>() {
        public ExtGrep createFromParcel(Parcel source) {
            return new ExtGrep(source);
        }

        public ExtGrep[] newArray(int size) {
            return new ExtGrep[size];
        }
    };
}
