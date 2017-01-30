package utils;

/**
 * Created by Anthony on 06/08/2016.
 * Package : experimentation .
 * Project : PhDTrack.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;

public class Word2DocLabelterator implements LabelAwareIterator {
    private static final long serialVersionUID = 1L;
    private Iterator<String> iter;
    private Map<String,String> docContentsMap;
    protected LabelsSource labelsSource;

    public Word2DocLabelterator(Map<String,String> doccontentsmap, @NonNull LabelsSource source) {
        docContentsMap = doccontentsmap;
        iter = docContentsMap.keySet().iterator();
        labelsSource = source;
    }

    @Override
    public boolean hasNextDocument() {
        return iter.hasNext();
    }

    @Override
    public LabelledDocument nextDocument() {

        LabelledDocument document = new LabelledDocument();
        if(iter.hasNext()) {
            String label = iter.next();
            String txt = docContentsMap.get(label);
            if (txt != null){
                if (txt.length() < 30) {
                    txt = " insufficient content to cluster this document";
                }
            }
            else {
                txt = "";
            }
            document.setContent(txt);
            document.setLabel(label);
        }
        return document;
    }

    @Override
    public void reset() {
        iter =  docContentsMap.keySet().iterator();
    }

    @Override
    public LabelsSource getLabelsSource() {
        return labelsSource;
    }

    public static class Builder {
        public Word2DocLabelterator build(Map<String,String> doccontentsmap) {
            List<String> labels = new ArrayList<>();
            for (String docid: doccontentsmap.keySet()) {
                labels.add(docid);
            }
            LabelsSource source = new LabelsSource(labels);
            return new Word2DocLabelterator(doccontentsmap, source);
        }
    }
}