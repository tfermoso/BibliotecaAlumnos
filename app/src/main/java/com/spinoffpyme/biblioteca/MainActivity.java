package com.spinoffpyme.biblioteca;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Stack;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity implements DetailsFragment.InterfaceDatos, FragmentManager.OnBackStackChangedListener {
    static public final String KEY_LIST = "numlist"; //clave para acceder al parámetro para mostrar
    //Añadimos
    static public final int FRAGMENT_ID = 0; //id para el fragmento, solo habrá uno
    static public final int NO_DETALLE = -1;

    Fragment fragmentAct = null; //puntero al fragmento, evitar buscarlo para borrar
    ///
    static public String KEY_RTN_VAL1 = "valor1"; //
    private static int COD_RTN_ACT = 0;
    Context ctx;
    int numLista = NO_DETALLE;

    /*
    Propiedades para gestionar la cola
     */
    static public final String CAB_TAG_FRAG = "frgm_";
    int numFrag = 0; //para generar el id del fragmento a recoger
    int numFragQueue = 0; //comprobar sii estamos eliminando un fragmento de la cola
    Stack<String> pilaTagFragmentos = new Stack<>();//pila para los tag de los fragmentos creados

    //Añadimos un menú al la barra de menú que viene por defecto
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_item,menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }
    //Añadimos eventos a los items del menú


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        //Gestión del elemento
        switch (item.getItemId()){
            case R.id.about:
                Toast.makeText(getApplication(),"Pulsado about",Toast.LENGTH_LONG).show();
                return true;
            case R.id.settings:
                Toast.makeText(getApplication(),"Pulsado settings",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        if (savedInstanceState != null) {
            //Estamos recreando la actividad, hay un Bundle de vuelta
            numLista = savedInstanceState.getInt(KEY_LIST, -1);

        }
        //Ponemos los datos en la lista
        ListView lv = (ListView) this.findViewById(R.id.lstLibros);

        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Datos.getNombres()));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //nos quedamos con el id pulsando para recrear
                numLista = position; //la posición es el índice del array
                System.out.println("Libro pulsado: "+numLista);

                /**
                 * Ahora en vez de lanzar la actividad cargaremos el fragment.
                 */
                /*
                //lanzar la otra actividad desde un intent
                Intent intent=new Intent(ctx, DetailActivity.class);
                intent.putExtra(KEY_LIST,numLista); //mandamos el número del libro a visualizar
                startActivityForResult(intent,COD_RTN_ACT); //abrimos y esperamos resultado
                */

                //Para simplificar creamos una función
                /*
                Vamos a detectar si estamos en portrait o landscape,

                 */
                if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                    System.out.println("Pantalla en portrait");
                }
                addDetalles(numLista);
            }
        });
        //si tenemos que recrear la actividad se carga el detalle que había
        //la primera vez noo cargará nada
        System.out.println("Cargamos numlista: " + numLista);
        if (numLista > NO_DETALLE) addDetalles(numLista);

        /*
        Hacemos la actividad receptora de los eventos del cambio de la cola de fragmentos
         */
        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener((FragmentManager.OnBackStackChangedListener) this);
    }

    //función de ayuda para la gestión de los fragmentos
    private void addDetalles(int numLista) {
        Fragment fragmentOld = fragmentAct;
        //gestionamos el fragmento
        //paso1
        FragmentManager fm = getFragmentManager();
        //paso2
        FragmentTransaction transicion = fm.beginTransaction();
        //paso3
        Fragment fragment = new DetailsFragment();
        fragmentAct = fragment; //nos quedamos para evitar la búsqueda para borrar
        //pasamos los parámetros
        Bundle data = new Bundle();
        data.putInt(KEY_LIST, numLista);
        fragment.setArguments(data);
        if (fragmentOld == null) { //si no hay fragmento se añade, sino se reemplaza
            //Nuevo
            transicion.add(R.id.frmDetalles, fragment, CAB_TAG_FRAG + String.valueOf(numFrag));
        } else {
            //guardamos el fragmento en la pila para que no se pierda
            transicion.addToBackStack(null);
            transicion.replace(R.id.frmDetalles, fragment, CAB_TAG_FRAG + String.valueOf(numFrag));
        }
        //Nuevo
        //guardar la tag en la pila en orden y preparar la siguiente tag
        pilaTagFragmentos.push(CAB_TAG_FRAG + String.valueOf(numFrag));
        numFrag++; //numero de la siguiente tag

        //paso4
        transicion.commit();
    }

    /*
    Ahora ya no tenemos onActivityResult
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && requestCode == COD_RTN_ACT) {
            //cuando vuelva la actiidad de forma correcta recogemos los valores y los mostramos
            StringBuffer sb = new StringBuffer();
            sb.append("De vuelta:\n");
            sb.append("Valor 1:" + intent.getStringExtra(KEY_RTN_VAL1) + "\n");
            sb.append("Valor 2:" + intent.getData().toString());
            Toast.makeText(ctx, sb.toString(), Toast.LENGTH_LONG).show();//mostramos en pantalla
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_LIST, numLista); //grabamos los datos para recrear la actividad
    }

    //método de la interfaz del fragmento para la comunicación de vuelta
    @Override
    public void onComunicateData(String val1, String val2) {
        StringBuffer sb = new StringBuffer();
        sb.append("De vuelta:\n");
        sb.append("Valor 1: " + val1 + "\n");
        sb.append("Valor 2: " + val2);
        Toast.makeText(ctx, sb.toString(), Toast.LENGTH_LONG).show(); //mostramos en pantalla
        //Cerramos el detalle
        removeDetalles();
    }

    /*
    La gestión de los fragmentos se hace desde la actividad, por eso creamos dos métodos de ayuda
    Uno para añadir un fragmento y otro para borrarlo
     */
    private void removeDetalles() {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transicion = fm.beginTransaction();
        transicion.remove(fragmentAct);
        fragmentAct = null;
        //Nuevo , simular la pulsación del botón BACK
        fm.popBackStack();

        //Comprobar si la pila esta vacia
        if(pilaTagFragmentos.isEmpty()){
            numLista=NO_DETALLE;
        }
        transicion.commit();
    }

    //Controlar la vuelta atrás en la cola de fragmentos
    //Recuperar el puntero al fragmento actual
    @Override
    public void onBackStackChanged() {


        FragmentManager fm = getFragmentManager();
        System.out.println("Pila: "+fm.getBackStackEntryCount()+" cola: "+numFragQueue+" fragAct: "+numFrag);
        if(fm.getBackStackEntryCount()<numFragQueue){
            //Estamos sacando de la cola si el número actual de fragmentos es mayor que el total
            numFrag--;
            pilaTagFragmentos.pop();//sacar la Tag del que estamos desencolando
            //conseguir la tag del siguiente en la cola, el que se va a mostrar
            String tag=pilaTagFragmentos.peek();
            fragmentAct=fm.findFragmentByTag(tag); //apuntar al mostrado para que funcione al volver
        }
        numFragQueue=fm.getBackStackEntryCount();//preparar comprobación de siguiente salida de la cola
    }
}
