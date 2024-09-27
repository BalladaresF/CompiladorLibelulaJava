/*
 * Comandos a utilizar en el CMD: 
 * java -jar LIBELULA.jar CalcInflac.LID
 * java -jar LIBELULA.jar Feo.LID
 * java -jar LIBELULA.jar Prueba.txt
 * java -jar LIBELULA.jar LIBELULATAREA1.LID
 * java -jar LIBELULA.jar LIBELULATAREA2.LID
 */
package Clases;

/**
 *
 */
public class Main {

    /**
     *
     */
    public static void main(String[] args) {
        String NombreArchivo="";
        if(args.length>0){                      //el programa busca ejecutarse desde el cmd, para lo cual se necesita args.
            NombreArchivo=args[0];
            ArchivoErrores Analisis = new ArchivoErrores();
            Analisis.AnalizarTexto(NombreArchivo);
            
            if(Analisis.getErrorDetectado() == true){
                System.out.println("\nAVISO: El código presenta errores. Para más información, revisar el archivo de errores.");
            }else{
                System.out.println("\nEl código no presenta errores.");
            }
            
            System.out.println("\nProcedimiento completado.");
        }
        else{
            System.out.println("No se indicó el nombre del archivo por analizar.");
        }
    }
}

//Los regex han sido testeados por medio de https://regex101.com/