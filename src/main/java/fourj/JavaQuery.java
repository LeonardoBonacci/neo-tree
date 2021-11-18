package fourj;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class JavaQuery
{
    private static final Path databaseDirectory = Path.of( "target/java-query-db" );
    String resultString;
    String columnsString;
    String nodeResult;
    String rows = "";

    public static void main( String[] args )
    {
        JavaQuery javaQuery = new JavaQuery();
        javaQuery.run();
    }

    void run()
    {
        clearDbPath();

        // tag::addData[]
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        GraphDatabaseService db = managementService.database( DEFAULT_DATABASE_NAME );

        try ( Transaction tx = db.beginTx())
        {
            Node myNode = tx.createNode();
            myNode.setProperty( "name", "my node" );
            tx.commit();
        }
        // end::addData[]

        // tag::execute[]
        try ( Transaction tx = db.beginTx();
              Result result = tx.execute( "MATCH (n {name: 'my node'}) RETURN n, n.name" ) )
        {
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Entry<String,Object> column : row.entrySet() )
                {
                    rows += column.getKey() + ": " + column.getValue() + "; ";
                }
                rows += "\n";
            }
        }
        // end::execute[]
        // the result is now empty, get a new one
        try ( Transaction tx = db.beginTx();
              Result result = tx.execute( "MATCH (n {name: 'my node'}) RETURN n, n.name" ) )
        {
            // tag::items[]
            Iterator<Node> n_column = result.columnAs( "n" );
            n_column.forEachRemaining( node -> nodeResult = node + ": " + node.getProperty( "name" ) );
            // end::items[]

            // tag::columns[]
            List<String> columns = result.columns();
            // end::columns[]
            columnsString = columns.toString();
            resultString = tx.execute( "MATCH (n {name: 'my node'}) RETURN n, n.name" ).resultAsString();
        }

        managementService.shutdown();
    }

    private void clearDbPath()
    {
        try
        {
            FileUtils.deleteDirectory( databaseDirectory );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }
}