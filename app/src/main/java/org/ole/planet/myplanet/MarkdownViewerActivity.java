package org.ole.planet.myplanet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import br.tiagohm.markdownview.MarkdownView;


public class MarkdownViewerActivity extends AppCompatActivity {

    private TextView mMarkdownNameTitle;
    private MarkdownView mMarkdownContent;
    private String filePath;
    private String fileName;

    public MarkdownViewerActivity() {
        filePath = new DashboardFragment().globalFilePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_viewer);
        declareElements();
        renderMarkdownFile();
    }

    private void declareElements() {
        mMarkdownNameTitle = (TextView) findViewById(R.id.markdownFileName);
        mMarkdownContent = (MarkdownView) findViewById(R.id.markdown_view);
    }

    private void renderMarkdownFile() {
        // File name to be viewed

        Intent markdownOpenIntent = getIntent();
        fileName = markdownOpenIntent.getStringExtra("TOUCHED_FILE");

        if (fileName != null && !fileName.isEmpty()) {
            mMarkdownNameTitle.setText(fileName);
            mMarkdownNameTitle.setVisibility(View.VISIBLE);
        }

        mMarkdownContent.loadMarkdownFromFile(new File(filePath, fileName));
    }
}