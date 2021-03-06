/**
 * 
 */
package uk.bl.wap.indexer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.CoreContainer;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.bl.wap.util.solr.WritableSolrRecord;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class WARCIndexerEmbeddedSolrTest {

	private String testWarc = "src/test/resources/wikipedia-mona-lisa/flashfrozen-jwat-recompressed.warc.gz";
	//private String testWarc = "src/test/resources/variations.warc.gz";
	//private String testWarc = "src/test/resources/TEST.arc.gz";
	
	private EmbeddedSolrServer server;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		// Note that the following property could be set through JVM level arguments too
		  System.setProperty("solr.solr.home", "src/main/solr/solr");
		  System.setProperty("solr.data.dir", "target/solr-test-home");
		  CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		  CoreContainer coreContainer = initializer.initialize();
		  server = new EmbeddedSolrServer(coreContainer, "");
		  // Remove any items from previous executions:
		  server.deleteByQuery("*:*");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		server.shutdown();
	}

	@Test
	public void test() throws SolrServerException, IOException, NoSuchAlgorithmException, TransformerFactoryConfigurationError, TransformerException {
		// Fire up a SOLR:
		SolrInputDocument document = new SolrInputDocument();
        document.addField("id", "1");
        document.addField("name", "my name");

        System.out.println("Adding document: "+document);
        server.add(document);
        server.commit();
        
        System.out.println("Querying for document...");
        SolrParams params = new SolrQuery("name:name");
        QueryResponse response = server.query(params);
        assertEquals(1L, response.getResults().getNumFound());
        assertEquals("1", response.getResults().get(0).get("id"));
        
        //  Now generate some Solr documents from WARCs:
		List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

		/*
		 *
    org.archive.format.gzip.GZIPFormatException: Invalid FExtra length/records
	at org.archive.format.gzip.GZIPFExtraRecords.readRecords(GZIPFExtraRecords.java:59)
	at org.archive.format.gzip.GZIPFExtraRecords.<init>(GZIPFExtraRecords.java:17)
	at org.archive.format.gzip.GZIPDecoder.parseHeader(GZIPDecoder.java:151)
	at org.archive.format.gzip.GZIPDecoder.parseHeader(GZIPDecoder.java:126)
	at uk.bl.wap.indexer.WARCIndexerEmbeddedSolrTest.test(WARCIndexerEmbeddedSolrTest.java:73)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:597)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:28)
	at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:30)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:263)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:68)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:47)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:300)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)

		FileInputStream is = new FileInputStream("src/test/resources/wikipedia-mona-lisa/flashfrozen.warc.gz");
		new GZIPDecoder().parseHeader(is);
		
		System.out.println("COMPRESSED? "+ArchiveUtils.isGzipped(is));
		

		 */
		
		WARCIndexer windex = new WARCIndexer();
		ArchiveReader reader = ArchiveReaderFactory.get( new File(testWarc));
		Iterator<ArchiveRecord> ir = reader.iterator();
		while( ir.hasNext() ) {
			ArchiveRecord rec = ir.next();
			WritableSolrRecord doc = windex.extract("",rec, true);
			if( doc != null ) {
				//WARCIndexer.prettyPrintXML(ClientUtils.toXML(doc.doc));
				//break;
				docs.add(doc.doc);
			}
			//System.out.println(" ---- ---- ");
		}

        server.add(docs);
        server.commit();

        // Now query:
        params = new SolrQuery("content_type:image*");
        //params = new SolrQuery("generator:*");
        response = server.query(params);
        for( SolrDocument result : response.getResults() ) {
        	for( String f : result.getFieldNames() ) {
        		System.out.println(f + " -> " + result.get(f));
        	}
        }
        assertEquals(21L, response.getResults().getNumFound());

	}

}
