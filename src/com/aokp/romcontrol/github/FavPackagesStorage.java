package com.aokp.romcontrol.github;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FavPackagesStorage {
    private static final Pattern COMPILE = Pattern.compile("\\|");
    private List<String> mFavProjects;
    File mFile = new File(Environment.getExternalStorageDirectory() + "/aokp", ".fav_packages");

    public FavPackagesStorage() {
        if (!mFile.getParentFile().exists()) {
            if (! mFile.mkdirs()) {
                throw new ExceptionInInitializerError(
                    "Path could not be created! Failing...  " + mFile.getParent());
            }
        }
        List<String> projs = getAllFavoritedProjects();
        mFavProjects = new ArrayList<String>(0);
        if (projs != null && projs.size() > 0) {
            mFavProjects = projs;
        }
    }

    public FavPackagesStorage addProject(String projectPath) {
        mFile.delete();
        mFavProjects.add(projectPath);
        writeListToFile(mFavProjects);
        return this;
    }

    public boolean removeProject(String projectPath) {
        if (!mFile.delete()) {
            return false;
        }
        for (int i = 0; mFavProjects.size() > i; i++) {
            if (projectPath.equals(mFavProjects.get(i))) {
                mFavProjects.remove(i);
                writeListToFile(mFavProjects);
                return true;
            }
        }
        return false;
    }

    public boolean isFavProject(String projPath) {
        for (String mFavProject : mFavProjects) {
            if (mFavProject.equals(projPath)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getFavProjects() {
        return Collections.unmodifiableList(this.mFavProjects);
    }

    @SuppressWarnings("ReturnOfNull")
    private List<String> getAllFavoritedProjects() {
        List<String> projects = new ArrayList<String>(0);
        try {
            String s = readFile(mFile);
            String[] projs = COMPILE.split(s);
            Collections.addAll(projects, projs);
            return projects;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static String readFile(File pathFile) throws IOException {
        FileInputStream stream = new FileInputStream(pathFile);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    private boolean writeListToFile(List<String> list) {
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(mFile));
            int counter = 0;
            for (String s : list) {
                counter++;
                if (s != null && !"".equals(s.trim())) {
                    br.write(s.trim());
                }
                if (counter != list.size()) {
                    br.write('|');
                }
            }
            br.close();
            return true;
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
        return false;
    }
}
