//package org.example;
//
//import javax.jcr.*;
//
//import org.apache.jackrabbit.commons.JcrUtils;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.StringField;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//
//public class App{
//
//    public static void main(String[] args) {
//        try {
//            // Establish a session with the repository
//            Repository repository = JcrUtils.getRepository("http://localhost:4502/crx/server");
//            SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
//            Session session = repository.login(credentials);
//
//            // Assuming you have a Node called "rootNode"
//            String nodePath = "/content/dam/2_Pub_Commitment_574x384_2-column-desktop.jpeg"; // Path to the node containing the metadata
//            Node rootNode = session.getNode(nodePath);
//
//            // Create an index writer
//            Directory indexDirectory = FSDirectory.open(Paths.get("index-directory"));
//            IndexWriterConfig writerConfig = new IndexWriterConfig();
//            IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);
//
//            // Extract and index metadata recursively
//            extractAndIndexMetadata(rootNode, indexWriter);
//
//            // Commit and close the index writer
//            indexWriter.commit();
//            indexWriter.close();
//
//            // Close the session
//            session.logout();
//        } catch (RepositoryException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void extractAndIndexMetadata(Node node, IndexWriter indexWriter) throws RepositoryException, IOException {
//        // Create a new Lucene document
//        Document document = new Document();
//
//        // Iterate over the properties of the node
//        PropertyIterator propertyIterator = node.getProperties();
//        while (propertyIterator.hasNext()) {
//            Property property = propertyIterator.nextProperty();
//
//            // Handle multi-valued properties
//            if (property.isMultiple()) {
//                // Get the values of the multi-valued property
//                javax.jcr.Value[] values = property.getValues();
//                for (javax.jcr.Value value : values) {
//                    // Add each value as a separate field
//                    document.add(new StringField(property.getName(), value.getString(), Field.Store.YES));
//                }
//            } else {
//                // Add single-valued property as a field
//                document.add(new StringField(property.getName(), property.getValue().getString(), Field.Store.YES));
//            }
//        }
//
//        // Add the document to the Lucene index
//        indexWriter.addDocument(document);
//
//        // Recursively process child nodes
//        if (node.hasNodes()) {
//            NodeIterator nodeIterator = node.getNodes();
//            while (nodeIterator.hasNext()) {
//                Node childNode = nodeIterator.nextNode();
//                extractAndIndexMetadata(childNode, indexWriter);
//            }
//        }
//        System.out.println(document);
//    }
//}
package org.example;

import javax.jcr.*;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class App {

    private static Document indexedDocument;

    public static void main(String[] args) {
        try {
            // Establish a session with the repository
            Repository repository = JcrUtils.getRepository("http://localhost:4502/crx/server");
            SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
            Session session = repository.login(credentials);

            // Assuming you have a Node called "rootNode"
            String nodePath = "/content/dam/2_Pub_Commitment_574x384_2-column-desktop.jpeg"; // Path to the node containing the metadata
            Node rootNode = session.getNode(nodePath);

            // Create an index writer
            Directory indexDirectory = FSDirectory.open(Paths.get("index-directory"));
            IndexWriterConfig writerConfig = new IndexWriterConfig();
            IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);

            // Create a new Lucene document
            Document document = new Document();

            // Extract and index metadata recursively
            extractAndIndexMetadata(rootNode, document);

            // Add the document to the Lucene index
            indexWriter.addDocument(document);

            // Commit and close the index writer
            indexWriter.commit();
            indexWriter.close();

            // Close the session
            session.logout();

            // Print the indexed document
            System.out.println(document);

        } catch (RepositoryException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractAndIndexMetadata(Node node, Document document) throws RepositoryException, IOException {
        // Iterate over the properties of the node
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();

            // Handle multi-valued properties
            if (property.isMultiple()) {
                // Get the values of the multi-valued property
                javax.jcr.Value[] values = property.getValues();
                for (javax.jcr.Value value : values) {
                    // Add each value as a separate field
                    document.add(new StringField(property.getName(), value.getString(), Field.Store.YES));
                }
            } else {
                // Add single-valued property as a field
                document.add(new StringField(property.getName(), property.getValue().getString(), Field.Store.YES));
            }
        }

        // Recursively process child nodes
        if (node.hasNodes()) {
            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                Node childNode = nodeIterator.nextNode();
                extractAndIndexMetadata(childNode, document);
            }
        }
    }
}

