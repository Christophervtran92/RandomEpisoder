package com.example.randomepisoder.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Scanner;

import com.example.randomepisoder.R;
import com.example.randomepisoder.databinding.FragmentMainBinding;

/**
 * To do:
 *      Decide on a good time to save to file instead of every time a episode is randomized
 *      figure out the jank between futurama and simpsons history
 *          Saving seems to work correctly now but Futurama side seems to
 *          double up the history list
 *
 *          Quickly clicking the history button can cause app to freeze/softlock
 *          where you can't touch the screen to leave the history page
 */
public class PlaceholderFragment extends Fragment {
    //List of episodes for each simpsons season
    private final int[] seasonsEpisodesSim =   {    13, 22, 24, 22, 22, 25, 25, 25, 25, 23,
                                                    22, 21, 22, 22, 22, 21, 22, 22, 20, 21,
                                                    23, 22, 22, 22, 22, 22, 22, 22, 21, 23,
                                                    22, 22, 22  };
    //List of episodes for each Futurama season
    private final int[] seasonsEpisodesFut = { 13, 19, 22, 18, 16, 26, 26 };
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int NUM_OF_SIMPSONS_SEASONS = 33;
    private static final int NUM_OF_FUTURAMA_SEASONS = 7;
    private static final int MAX_INPUT_LINES = 30;
    private static final String SIMPSONS_HISTORY_FILE = "history_S.txt";
    private static final String FUTURAMA_HISTORY_FILE = "history_F.txt";
    private PageViewModel pageViewModel;
    private FragmentMainBinding binding;
    private File file;
    private PopupWindow historyWindow;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy hh:mm:ss");
    private LinkedList<String> tempHistory_S = new LinkedList<String>();
    private LinkedList<String> tempHistory_F = new LinkedList<String>();
    private Scanner scan;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);


        //Code for creating a file to store history of results
        //Simpsons history
        file = new File(getActivity().getFilesDir(), SIMPSONS_HISTORY_FILE);
        if(file.exists()) {
            try {
                //Read in data from save file and store temporarily into tempHistory
                scan = new Scanner(file);
                while(scan.hasNextLine()) {
                    System.out.println("Reading from " + SIMPSONS_HISTORY_FILE);
                    tempHistory_S.add(scan.nextLine() + "\n");
                }
                System.out.println("Simpsons " + tempHistory_S.toString());
                scan.close();
            } catch(Exception e) {
                System.err.println("File " + SIMPSONS_HISTORY_FILE + " not found");
            }
        } else {
            try {
                //Create a new file if one doesn't already exist
                file.createNewFile();
            } catch(Exception e) {
                System.out.println("File creation error!");
            }
            System.out.println("It does not exists...");
        }

        //Futurama history
        file = new File(getActivity().getFilesDir(), FUTURAMA_HISTORY_FILE);
        if(file.exists()) {
            try {
                //Read in data from save file and store temporarily into tempHistory
                scan = new Scanner(file);
                while(scan.hasNextLine()) {
                    System.out.println("Reading from " + FUTURAMA_HISTORY_FILE);
                    tempHistory_F.add(scan.nextLine() + "\n");
                }
                System.out.println("Futurama " + tempHistory_F.toString());
                scan.close();
            } catch(Exception e) {
                System.err.println("File " + FUTURAMA_HISTORY_FILE + " not found");
            }
        } else {
            try {
                //Create a new file if one doesn't already exist
                file.createNewFile();
            } catch(Exception e) {
                System.out.println("File creation error!");
            }
            System.out.println("It does not exists...");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView resultsView = binding.sectionLabel;
        final Button button = binding.button;

        //If Tab is 2 change the theme to match
        if(getArguments().getInt(ARG_SECTION_NUMBER) > 1) {
            binding.constraintLayout.setBackgroundColor(getResources().getColor(R.color.ship_teal));
            resultsView.setBackgroundColor(getResources().getColor(R.color.futurama_blue));
            resultsView.setTextColor(getResources().getColor(R.color.futurama_yellow));
            resultsView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.futuramabold));
            button.setBackgroundColor(getResources().getColor(R.color.futurama_pale_blue));
            button.setTextColor(getResources().getColor(R.color.futurama_red));
            button.setShadowLayer(1, 3, 3, getResources().getColor(R.color.yellow));
            button.setTypeface(ResourcesCompat.getFont(getContext(), R.font.futuramabold));
        }

        pageViewModel.getText().observe(getViewLifecycleOwner(), resultsView::setText);

        //Set up functionality for the random button
        binding.button.setOnClickListener(view -> {
            //Set up button click vibrate functionality
            try {
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
            } catch(Exception e) {
                System.err.println("Vibrator error");
            }

            //Picks a random episode number and stores it into the live data variable mEpisode
            int season;
            int episode;

            //ARG_SECTION_NUMBER == 1 if Simpsons, 2 if Futurama
            if(getArguments().getInt(ARG_SECTION_NUMBER) < 2) {
                Random rand = new Random();                                 //Simpsons
                season = rand.nextInt(NUM_OF_SIMPSONS_SEASONS) + 1;         //Randomly find season
                episode = rand.nextInt(seasonsEpisodesSim[season - 1]) + 1; //Randomly find episode
                pageViewModel.setEpisode(episode);                          //episode in mEpisode, kinda useless
            } else {
                Random rand = new Random();                                 //Futurama
                season = rand.nextInt(NUM_OF_FUTURAMA_SEASONS) + 1;         //Randomly find season
                episode = rand.nextInt(seasonsEpisodesFut[season - 1]) + 1; //Randomly find episode
                pageViewModel.setEpisode(episode);                          //episode in mEpisode
            }

            //Code for writing to file
            Date date = new Date();
            String result;
            if(getArguments().getInt(ARG_SECTION_NUMBER) < 2) {
                result = "S (" + dateFormatter.format(date) + ") Season: " + season + ", Episode: " + episode + '\n';
                tempHistory_S.add(0, result);
                if(tempHistory_S.size() > MAX_INPUT_LINES) {    //Do not exceed the max allowable lines of history
                    tempHistory_S.removeLast();
                }
                updateHistory(ARG_SECTION_NUMBER);
            } else {
                result = "F (" + dateFormatter.format(date) + ") Season: " + season + ", Episode: " + episode + '\n';
                tempHistory_F.add(0, result);
                if(tempHistory_F.size() > MAX_INPUT_LINES) {    //Do not exceed the max allowable lines of history
                    tempHistory_F.removeLast();
                }
                updateHistory(ARG_SECTION_NUMBER);
            }

            //When a change in mEpisode is observed, update text on screen
            pageViewModel.getEpisode().observe(getViewLifecycleOwner(), i -> {
                resultsView.setText(getString(R.string.results_text) + "\n\nSeason: " + season + "\t\t\t\t\t\t\tEpisode: " + i);
            });
        });

        //Set up bottom right button for history popup view
        //Ref: https://stackoverflow.com/questions/5944987/how-to-create-a-popup-window-popupwindow-in-android
        binding.floatingActionButton.setOnClickListener(view -> {
            //Inflate the history_main layout
            View historyView = inflater.inflate(R.layout.history_main, null);
            //Use historyView for new popup window
            historyWindow = new PopupWindow(historyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            if(Build.VERSION.SDK_INT >= 21) {
                historyWindow.setElevation(1.0f);
            }
            String strHistory = "";
            if(getArguments().getInt(ARG_SECTION_NUMBER) < 2) {
                for (String i : tempHistory_S) {
                    strHistory += i;
                }
                //Display history window at center of screen
                TextView historyTextView = historyView.findViewById(R.id.history_text_view);
                historyTextView.setText(strHistory);
            } else {
                for (String i : tempHistory_F) {
                    strHistory += i;
                }
                //Display history window at center of screen
                TextView historyTextView = historyView.findViewById(R.id.history_text_view);
                historyTextView.setText(strHistory);
            }
            historyWindow.showAtLocation(root, Gravity.CENTER, 0, 0);

            //Click/touch view to close window
            historyView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    historyWindow.dismiss();
                    return true;
                }
            });
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Method to update the history
    //For some reason as a private method it still works inside public methods
    private void updateHistory(String section) {
        OutputStreamWriter toFileS;
        OutputStreamWriter toFileF;

        //Update only the simpsons history when on the simpsons tab and only the futurama history on the futurama tab
        if (getArguments().getInt(section) < 2) {
            try {
                toFileS = new OutputStreamWriter(getContext().openFileOutput(SIMPSONS_HISTORY_FILE, Context.MODE_PRIVATE));
                //Write the history to file
                for (String i : tempHistory_S) {
                    toFileS.append(i);
                }
                System.out.println(SIMPSONS_HISTORY_FILE + " saved");
                toFileS.close();
            } catch (Exception e) {
                System.err.println("Write to " + SIMPSONS_HISTORY_FILE + " failed.");
            }
        } else {
            try {
                toFileF = new OutputStreamWriter(getContext().openFileOutput(FUTURAMA_HISTORY_FILE, Context.MODE_PRIVATE));
                for (String i : tempHistory_F) {
                    toFileF.append(i);
                }
                System.out.println(FUTURAMA_HISTORY_FILE + " saved");
                toFileF.close();
            } catch (Exception e) {
                System.err.println("Write to " + FUTURAMA_HISTORY_FILE + " failed.");
            }
        }
    }
}