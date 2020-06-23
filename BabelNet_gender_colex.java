/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package babeltry;

import com.babelscape.util.UniversalPOS;
import java.io.IOException;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetQuery;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetRelation;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.babelnet.data.BabelLemma;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.kb.Gloss;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;

/**
 *
 * @author Mi
 */
public class BabelNet_gender_colex {

    public static void main(String[] args) throws IOException, InterruptedException {
        BabelNet bn = BabelNet.getInstance();
        
        Scanner myScanner = new Scanner(System.in);
        String filename = "female_hyponyms_omwiki_selected_langs_2.tsv";
        createFile(filename);
        
        List<Language> langs_list = Arrays.asList(new Language[] {Language.FR, Language.EN, Language.FI, Language.CS, Language.HU, Language.TR, Language.TA, Language.HE, Language.KO, Language.HY, Language.TH, Language.TL});

        OutputStream outputStream = new FileOutputStream(filename, true);
        OutputStreamWriter myWriter = new OutputStreamWriter(outputStream, "UTF-8");
        myWriter.write("synset\thyponym\tlang\tgloss\n");
        BabelNetQuery query = new BabelNetQuery.Builder("female")
                            .from(Language.EN)
                            .POS(UniversalPOS.NOUN)
                            .sources(Arrays.asList(BabelSenseSource.OMWIKI))
                            .build();
        List<BabelSynset> synsets_list = bn.getSynsets(query); // 1
        Set<BabelSynset> seen = new HashSet();
        seen.addAll(synsets_list);
        System.out.println("Length of synsets: " + synsets_list.size());
        for (BabelSynset synset : synsets_list) {
            String synset_lemma = synset.getMainSense(Language.EN).get().getFullLemma(); // 1
            System.out.println("Synset " + synset_lemma);
            System.out.println(synset.getGlosses());
            System.out.println(synset.getCategories());
            System.out.print("Want to take it? ");
            int decision_synset = myScanner.nextInt();
            if (decision_synset == 1){
                List<BabelSynsetRelation> edges = synset.getOutgoingEdges(BabelPointer.ANY_HYPONYM); // 1
                System.out.println("Length of hyponyms: " + edges.size());
                for (BabelSynsetRelation edge : edges) {
                    String hyponym_lemma = "";
                    int decision_hyponym = 0;
                    for (List<Language> langs : Lists.partition(langs_list, 3)){
                        BabelSynset hyp = bn.getSynset(langs, edge.getBabelSynsetIDTarget()); // 1
                        if (langs.contains(Language.EN)){
                            if (seen.contains(hyp)){
                                System.out.println("Already seen you!");
//                                continue;
                            } else {
                                seen.add(hyp);
                                try{
                                    hyponym_lemma = hyp.getMainSense(Language.EN).get().getFullLemma();
                                    System.out.println("Hyponym " + hyponym_lemma);
                                    System.out.println(hyp.getCategories());
                                    System.out.println(hyp.getGlosses());
                                } catch (NoSuchElementException e) {
                                            System.out.println("An error occurred.");
                                            e.printStackTrace();
                                        }
                                System.out.print("Want to take hyponym? ");
                                decision_hyponym = 1;
                            }
                        }
                        if (decision_hyponym == 1){
                            Set<Language> langs_hyp = hyp.getLanguages();
                            System.out.println("Langs:" + langs_hyp);
                            for (Language lang: langs_hyp){
                                List<BabelLemma> lemmas_list = hyp.getLemmas(lang);
                                System.out.println("Length of lemmas: " + lemmas_list.size());
                                for (BabelLemma lemma : lemmas_list) {
                                    try{
                                        String line = synset_lemma + "\t" + hyponym_lemma + "\t" + lang.getName() + "\t" + lemma.getLemma() + "\n";
                                        myWriter.write(line);
                                        System.out.print(line);
                                    } catch (NoSuchElementException e) {
                                        System.out.println("An error occurred.");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void createFile(String filename) {
        try {
            File myObj = new File(filename);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
