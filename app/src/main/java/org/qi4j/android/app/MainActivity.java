package org.qi4j.android.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.valueserialization.jackson.JacksonValueSerializationAssembler;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        if( savedInstanceState == null )
        {
            getFragmentManager().beginTransaction()
                    .add( R.id.container, new PlaceholderFragment(), "main" )
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected( item );
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            return inflater.inflate( R.layout.fragment_main, container, false );
        }

        @Override
        public void onViewCreated( View view, Bundle savedInstanceState )
        {
            TextView output = (TextView) view.findViewById( R.id.QI4J_OUTPUT );
            Application qi4jApp = null;
            try
            {
                // Assemble a simple Application with a single ValueComposite
                SingletonAssembler assembler = new SingletonAssembler()
                {
                    @Override
                    public void assemble( ModuleAssembly moduleAssembly ) throws AssemblyException
                    {
                        new JacksonValueSerializationAssembler().assemble( moduleAssembly );
                        moduleAssembly.values( TestValue.class );
                    }
                };
                qi4jApp = assembler.application();
                Module module = assembler.module();

                // Create a new TestValue
                ValueBuilder<TestValue> builder = module.newValueBuilder( TestValue.class );
                builder.prototype().text().set("It works!");
                TestValue value = builder.newInstance();

                // Log and display the value text
                Log.e("QI4J", value.text().get());
                output.append( "\n" + value.text().get() );
            }
            catch( AssemblyException | ActivationException e )
            {
                // Log and display exception stacktrace
                Log.e("QI4J", e.getMessage(), e);
                output.append( "\n" + stacktrace(e) );
            }
            finally
            {
                if( qi4jApp!=null )
                {
                    try
                    {
                        // Passivate Application
                        qi4jApp.passivate();
                    }
                    catch( PassivationException e )
                    {
                        // Log and display exception stacktrace
                        Log.e("QI4J", e.getMessage(), e);
                        output.append( "\n" + stacktrace( e ) );
                    }
                }
            }
        }
    }

    public static interface TestValue
    {
        Property<String> text();
    }

    private static String stacktrace( Throwable throwable )
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        throwable.printStackTrace( pw );
        pw.flush();
        return sw.toString();
    }

}
