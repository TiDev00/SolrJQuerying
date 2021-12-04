import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Searcher {
    /**
     * The static solrClient instance.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("======================== SolrJ Querying ========================");
        Searcher searcher = new Searcher();
        searcher.queryingByUsingSolrParams("src/main/resources/short_q.csv");
    }

    /**
     * The Solr instance URL running on localhost
     */
    private static final String SOLR_CORE_URL = "http://localhost:8983/solr/test";

    /**
     * The static solrClient instance.
     */
    private static final SolrClient solrClient = getSolrClient();

    /**
     * Configures SolrClient parameters and returns a SolrClient instance.
     *
     * @return a SolrClient instance
     */
    private static SolrClient getSolrClient() {
        return new HttpSolrClient.Builder(SOLR_CORE_URL).withConnectionTimeout(5000).withSocketTimeout(3000).build();
    }

    /**
     * Print the given documents to standard output.
     *
     * @param documents the search results
     */
    private static void printResults(String qNum, SolrDocumentList documents) {
//        System.out.printf("Found %d documents\n", documents.getNumFound());
        for (SolrDocument document : documents) {
            final String docno = (String) document.getFirstValue("docno");
            final float score = (Float) document.getFirstValue("score");
            System.out.print(qNum + "\tQ0\t" + docno + "\t" + score + "\tSTANDARD\n");
        }
    }

    /**
     * Querying documents by using SolrParams.
     */
    public void queryingByUsingSolrParams(String myFile) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(myFile));
        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] datas = row.split(";");

            // constructs a MapSolrParams instance
            final Map<String, String> queryParamMap = new HashMap<>();
            queryParamMap.put("q", datas[1]);
            queryParamMap.put("q.op", "OR");
            queryParamMap.put("fl", "docno,score");
            queryParamMap.put("rows", "500");
            queryParamMap.put("df", "text");
            queryParamMap.put("sort", "score desc");
            MapSolrParams queryParams = new MapSolrParams(queryParamMap);

            // sends search request and gets the response
            QueryResponse response = null;
            try {
                response = solrClient.query(queryParams);
            } catch (SolrServerException | IOException e) {
                System.err.printf("Failed to search documents: %s", e.getMessage());
            }

            // print results to stdout
            if (response != null) {
                printResults(datas[0], response.getResults());
            }
        }
        csvReader.close();
    }
}