/*
 * En esta clase se analiza el archivo. Todos los errores se detectan aquí.
 */
package Clases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 */

public class ArchivoErrores {
  boolean ErrorDetectado = false; //En caso de detectar errores, avisar que el código no se puede ejecutar.
  List<String> Identificadores = new ArrayList<>();
  List<String> Tipos = new ArrayList<>();
  
  TablaSimbolos.Tipos ComandosLIBELULA = TablaSimbolos.Tipos.ReservadaLIBELULA;
  TablaSimbolos.Tipos ComandosMODULA2 = TablaSimbolos.Tipos.ReservadaMODULA2;
  TablaSimbolos.Tipos TiposVariables = TablaSimbolos.Tipos.TiposVariables;
  
  String[] ValoresLIBELULA = this.ComandosLIBELULA.Patron.split("(\\(|\\)|\\|)");
  String[] ValoresMODULA2 = this.ComandosMODULA2.Patron.split("(\\(|\\)|\\|)");
  String[] ListaVariables = this.TiposVariables.Patron.split("(\\(|\\)|\\|)");
  
  
  
  public void AnalizarTexto(String Nombre) {
    int Contador = 1;
    
    boolean REPEATdetectado = false;
    boolean UNTILdetectado = false;
    
    boolean IFdetectado = false;
    boolean IFComandoDetectado = false;
    boolean ELSEdetectado = false;
    boolean ELSEComandoDetectado = false;
    boolean ENDIFdetectado = false;
    
    boolean MODULEdetectado = false;
    boolean BEGINdetectado = false;
    boolean ENDdetectado = false;
    boolean VARdetectado = false;
    
    try {
        BufferedReader br = new BufferedReader(new FileReader(Nombre));

        String[] DivisionNombre = Nombre.split("\\.");
        String NombreOriginal = DivisionNombre[0];    //para que, al generar el archivo de errores, no sea "feo.LID-errores", sino "feo-errores".

        File CrearArchivo = new File(NombreOriginal + "-errores.txt");
        FileWriter ArchivoErrores = new FileWriter(NombreOriginal + "-errores.txt");
        DecimalFormat df = new DecimalFormat("00000");

        String NombreModuloInicial = "";
        String NombreModulo = "";
        String NombreEnd = "";

        boolean LeyendoComentario = false;
        int ContadorComentarios = 0;

        System.out.println("\nArchivo por analizar: " + NombreOriginal);
        String Linea;

        while ((Linea = br.readLine()) != null) {
            ArchivoErrores.write(df.format(Contador)+"\t"+Linea+"\n");  //escribe todas las líneas en el archivo de errores.
            Contador++;
            
            //Detectar si el código supera las 99999 líneas:
            if (Contador > 99999) {
                ArchivoErrores.write("\tERROR 001: el archivo supera las 99999 líneas\n");
                this.ErrorDetectado = true;
                break;  //luego del break, no se leerá ninguna línea. No se espera que algún código llegue a tener más de 99999 líneas.
            } 
            
            //detectar si la línea es superior a 100 caracteres:
            if (Linea.length() > 100){
                EscribirError("\tERROR 002: la línea superó 100 caracteres.\n", ArchivoErrores); 
            }
            
            //manejo de comentarios:
            //se ha empleado el método trim() para ignorar espacios.
            if (LeyendoComentario == true){
                ContadorComentarios++;  //para verificar la cantidad de líneas de los comentarios.
            }
            
            if (ContadorComentarios > 10) {
                EscribirError("\tERROR 003: el comentario ha superado las 10 líneas[(*].\n", ArchivoErrores);
                LeyendoComentario = false;
                ContadorComentarios = 0;
            } 
            if (!Linea.trim().startsWith("(*") || !Linea.trim().endsWith("*)")) {
                if (Linea.trim().equals("(*") && !LeyendoComentario) {
                    LeyendoComentario = true;
                }else
                    if (Linea.trim().equals("*)") && LeyendoComentario == true) {
                        LeyendoComentario = false;
                        ContadorComentarios = 0;
                    }else
                        if (Linea.trim().startsWith("(*") && !Linea.trim().endsWith("*)") && !LeyendoComentario) {
                            EscribirError("\tERROR 004: el comentario no se ha cerrado [(*].\n", ArchivoErrores);
                        } else
                            if (!Linea.trim().startsWith("(*") && Linea.trim().endsWith("*)") && !LeyendoComentario) {
                              EscribirError("\tERROR 005: el comentario no se ha abierto [*)].\n", ArchivoErrores);
                        }else
                            if (!LeyendoComentario || ContadorComentarios > 10) {
                                
                                String PrimerValor = "";
                                
                                if (!Linea.isBlank()) {
                                  String[] SplitLinea = Linea.trim().replaceAll(";", "").split("[.:=\\s\\(\\)]+", 2);
                                  PrimerValor = SplitLinea[0];
                                } 
                                
                                //Manejo de MODULE
                                if (!MODULEdetectado && !Linea.isBlank() && !Linea.contains("MODULE") && !EsComandoDeMODULA2(PrimerValor)){
                                    EscribirError("\tERROR 006: Comando no válido antes de MODULE [" + Linea + "].\n", ArchivoErrores); 
                                }
                                if(!EsComandoDeLIBELULA(PrimerValor) && !EsComandoDeMODULA2(PrimerValor) && !MODULEdetectado){
                                    EscribirError("\tERROR 007: Comando no válido de LIBELULA ni de MODULA2 [" + PrimerValor + "].\n", ArchivoErrores); 
                                }
                                if (Linea.trim().startsWith("MODULE")) {
                                    boolean ConfirmarModulo = true;
                                    String[] Dividir = Linea.split(";");
                                    NombreModuloInicial = Dividir[0].replaceAll("MODULE|\\;| ", "");
                                    
                                    if (MODULEdetectado == true) {
                                        EscribirError("\tERROR 008: comando no debe invocarse más de una vez [MODULE].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                    } 
                                    if (!Linea.trim().endsWith(";")) {
                                        EscribirError("\tERROR 009: comando debe terminar con punto y coma [MODULE].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                    }
                                    //Análisis del identificador de MODULE
                                    if (NombreModuloInicial.length() > 20) {
                                        EscribirError("\tERROR 010: identificador es mayor a 20 caracteres [" + NombreModuloInicial + "].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                    } 
                                    if (ContieneCaracteresEspeciales(NombreModuloInicial)) {
                                        EscribirError("\tERROR 011: identificador contiene caracteres especiales [" + NombreModuloInicial + "].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                    } 
                                    if (EsComandoDeLIBELULA(NombreModuloInicial)) {
                                        EscribirError("\tERROR 012: El identificador es un comando de LIBELULA [" + NombreModuloInicial + "].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                    } 
                                    if (EsComandoDeMODULA2(NombreModuloInicial)) {
                                        EscribirError("\tERROR 013: El identificador es un comando de MODULA2 [" + NombreModuloInicial + "].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                     } 
                                    if (EmpiezaConNumero(NombreModuloInicial)) {
                                        EscribirError("\tERROR 014: El identificador comienza con un número [" + NombreModuloInicial + "].\n", ArchivoErrores);
                                        ConfirmarModulo = false;
                                    } 
                                    if (ConfirmarModulo == true) {
                                        //Si no hay errores, se acepta MODULE  y su identificador
                                        MODULEdetectado = true;
                                        NombreModulo = NombreModuloInicial;
                                    } 
                              } else 
                                //Manejo de BEGIN:
                                if (Linea.trim().contains("BEGIN") && BeginTieneMasTexto(Linea.trim())) {
                                    EscribirError("\tERROR 015: comando no debe tener más texto en la línea [BEGIN].\n", ArchivoErrores);
                                } else 
                                if (Linea.trim().equals("BEGIN")) {
                                    if (BEGINdetectado == true)
                                    EscribirError("\tERROR 016: comando no debe invocarse más de una vez [BEGIN].\n", ArchivoErrores); 
                                    BEGINdetectado = true;
                              } else 
                                if (MODULEdetectado == true && !BEGINdetectado && !EsComandoDeMODULA2(PrimerValor) && !PrimerValor.equals("VAR") && !Linea.isBlank()) {
                                    if (!VARdetectado){
                                        EscribirError("\tERROR 017: Comando no válido antes de BEGIN [" + Linea + "].\n", ArchivoErrores); 
                                    }
                                } else
                                //Manejo de VAR:
                                if (Linea.trim().equals("VAR")) {
                                    if (VARdetectado == true)
                                        EscribirError("\tERROR 018: Comando no puede declararse más de una vez [" + Linea + "].\n", ArchivoErrores); 
                                    VARdetectado = true;
                                    if (BEGINdetectado == true)
                                        EscribirError("\tERROR 019: Comando no válido después de BEGIN [" + Linea + "].\n", ArchivoErrores); 
                                } 
                              if (!Linea.isBlank() && !Linea.trim().equals("VAR") && VARdetectado == true && !BEGINdetectado) {
                                boolean bool = false;
                                for (String Comandos1 : this.ValoresLIBELULA) {
                                    for (String Comandos2 : this.ValoresMODULA2) {
                                        if ((Linea.startsWith(Comandos1) || Linea.startsWith(Comandos2)) && !Linea.contains(":") && !bool) {
                                            EscribirError("\tERROR 020: Comando no válido antes de BEGIN [" + Linea + "].\n", ArchivoErrores);
                                            bool = true;
                                        } 
                                    } 
                                }
                                
                                //Declaración de variables:
                                int Contar = (Linea.split(";")).length - 1;
                                if (Contar > 1) {
                                    String[] partes = Linea.split(";");
                                    for (int i = 0; i < partes.length; i++) {
                                        partes[i] = partes[i].trim() + ";";
                                        AnalizarDeclaracionDeVariables(partes[i], ArchivoErrores);
                                    } 
                                } else {
                                    AnalizarDeclaracionDeVariables(Linea, ArchivoErrores);
                                } 
                              } 
                              boolean ConfirmarError = false;
                              if (Linea.contains(":") && BEGINdetectado == true) {
                                    String[] Seccion = Linea.split(":");
                                    for (String Variable : this.ListaVariables) {
                                        if (!Variable.equals("") && Seccion[1].trim().contains(Variable) && !ConfirmarError && !Linea.contains("=") && !Linea.contains("(") && !Linea.contains(")")) {
                                            EscribirError("\tERROR 021: Variable declarada luego de BEGIN [" + Linea + "].\n", ArchivoErrores);
                                            ConfirmarError = true;
                                        } 
                                    } 
                              } 
                              if (!EsComandoDeLIBELULA(PrimerValor) && !EsComandoDeMODULA2(PrimerValor) && !IdentificadorExiste(PrimerValor) && !PrimerValor.equals("") && BEGINdetectado == true){
                                  EscribirError("\tERROR 022: Comando no válido de LIBELULA ni de MODULA2 [" + PrimerValor + "].\n", ArchivoErrores); 
                              }
                              
                              //Asignación de variables:
                              int Contar = (Linea.split(";")).length - 1;
                              if (Contar > 1) {
                                    String[] partes = Linea.split(";");
                                    for (int i = 0; i < partes.length; i++) {
                                        partes[i] = partes[i].trim() + ";";
                                        if(!partes[i].trim().equals(";")){
                                            AnalizarAsignacionDeVariables(partes[i], BEGINdetectado, PrimerValor, ArchivoErrores);
                                        }
                                    } 
                                } else {
                                    AnalizarAsignacionDeVariables(Linea, BEGINdetectado, PrimerValor, ArchivoErrores);
                                } 
                              
                              //Manejo de REPEAT:
                              if(Linea.contains("REPEAT") && REPEATTieneMasTexto(Linea.trim())){
                                  EscribirError("\tERROR 023: Comando no debe tener más texto en la línea [REPEAT].\n", ArchivoErrores); 
                              }
                              if(Linea.trim().equals("REPEAT")){
                                  REPEATdetectado = true;
                              }
                              if(Linea.trim().startsWith("UNTIL") && REPEATdetectado == false){
                                  EscribirError("\tERROR 024: Comando no válido fuera de REPEAT [UNTIL].\n", ArchivoErrores); 
                              }
                              if(REPEATdetectado == true && !Linea.equals("REPEAT")){
                                  if(Linea.contains("IF")){
                                      EscribirError("\tERROR 025: IF usado dentro de REPEAT [IF].\n", ArchivoErrores); 
                                  }
                                  if(Linea.contains("REPEAT")){
                                      EscribirError("\tERROR 026: REPEAT usado dentro de REPEAT [REPEAT].\n", ArchivoErrores); 
                                  }
                                  if(Linea.startsWith("UNTIL")){
                                      boolean UNTILaceptado = true;
                                      if(UNTILdetectado == true){
                                          EscribirError("\tERROR 027: comando no se puede invocar más de una vez [UNTIL].\n", ArchivoErrores); 
                                      }
                                      if(!Linea.trim().endsWith(";")){
                                          EscribirError("\tERROR 028: Comando debe terminar con punto y coma [UNTIL].\n", ArchivoErrores); 
                                          UNTILaceptado = false;
                                      }
                                      if(Linea.trim().equals("UNTIL") || Linea.trim().equals("UNTIL;")){
                                          EscribirError("\tERROR 028: Comando debe tener condición [UNTIL].\n", ArchivoErrores); 
                                          UNTILaceptado = false;
                                      }
                                      if(Linea.contains("(") || Linea.contains(")")){
                                          EscribirError("\tERROR 030: Comando no debe tener paréntesis [UNTIL].\n", ArchivoErrores); 
                                          UNTILaceptado = false;
                                      }
                                      
                                      //Análisis de identificadores:
                                      String[] ListaValores = Linea.split("\\s*(UNTIL|<|>|<=|>=|=|!=)\\s*");
                                      for(String Valores : ListaValores){
                                         if(!Valores.equals("")){
                                             Valores = Valores.trim().replaceAll(";", "");
                                             //System.out.println(Valores);
                                             if(!IdentificadorExiste(Valores) && !EsComandoDeMODULA2(Valores) && !EsNumero(Valores)){
                                                 //no se ha enviado a escribir un error porque este se escribirá desde la sección de tokens identificadores.
                                                 UNTILaceptado = false;
                                             }
                                             if(EsComandoDeLIBELULA(Valores)){
                                                 EscribirError("\tERROR 031: Comando no permitido en la condición de UNTIL ["+Valores+"].\n", ArchivoErrores); 
                                                 UNTILaceptado = false;
                                             }
                                         }
                                       }
 
                                      if(UNTILaceptado == true){
                                          UNTILdetectado = true;
                                          REPEATdetectado = false;
                                      }
                                  } 
                              }
                              
                              //Manejo de IF:
                              if(Linea.contains("IF")){
                                  boolean IFaceptado = true;
                                  if(Linea.trim().endsWith(";")){
                                      EscribirError("\tERROR 032: Comando no debe terminar con punto y coma [IF].\n", ArchivoErrores); 
                                      IFaceptado = false;
                                  }
                                  if(!IFTieneMasTexto(Linea.trim())){
                                      EscribirError("\tERROR 033: Comando debe tener condición [IF].\n", ArchivoErrores); 
                                      IFaceptado = false;
                                  }
                                  if(!Linea.contains("(") || !Linea.contains(")")){
                                      EscribirError("\tERROR 034: Comando debe tener ambos paréntesis [IF].\n", ArchivoErrores); 
                                      IFaceptado = false;
                                  }
                                  if(!Linea.trim().endsWith("THEN")){
                                      EscribirError("\tERROR 035: Comando debe terminar con THEN [IF].\n", ArchivoErrores);
                                      IFaceptado = false;
                                  }
                                  
                                  if(IFdetectado == true){
                                      EscribirError("\tERROR 036: IF usado dentro de IF [IF].\n", ArchivoErrores); 
                                  }
                                  if(IFdetectado == true && Linea.contains("REPEAT")){
                                      EscribirError("\tERROR 037: REPEAT usado dentro de IF [REPEAT].\n", ArchivoErrores); 
                                  }
                                  
                                  //Análisis de identificadores:
                                  Pattern pattern = Pattern.compile("\\((.*?)\\)");
                                  Matcher matcher = pattern.matcher(Linea);
  
                                  while (matcher.find()) {
                                      String Division = matcher.group(1).replaceAll("<|>|<=|>=|=|!=", "");
                                      String[] ListaValores = Division.split(" ");
                                      for(String Valores : ListaValores){
                                        if(!Valores.equals("")){
                                            //System.out.println(Valores);
                                            if(!IdentificadorExiste(Valores.trim()) && !EsComandoDeMODULA2(Valores.trim()) && !EsNumero(Valores.trim())){
                                                //no se ha enviado a escribir un error porque este se escribirá desde la sección de tokens identificadores.
                                                IFaceptado = false;
                                            }
                                            if(EsComandoDeLIBELULA(Valores.trim())){
                                                EscribirError("\tERROR 038: Comando no permitido en la condición de IF ["+Valores.trim()+"].\n", ArchivoErrores); 
                                                IFaceptado = false;
                                            }
                                        }
                                      }
                                      if(Division.equals("")){
                                          EscribirError("\tERROR 111: No se ha escrito nada entre paréntesis ["+Division+"].\n", ArchivoErrores); 
                                          IFaceptado = false;
                                          if(IFdetectado == true){
                                            EscribirError("\tERROR 039: IF usado dentro de IF [IF].\n", ArchivoErrores); 
                                          }
                                      } 
                                  }
                                  
                                  if(IFaceptado == true){
                                      IFdetectado = true;
                                  }
                              }
                              if(IFdetectado == true && !Linea.contains("IF") && !Linea.isBlank()){
                                  IFComandoDetectado = true;
                              }
                              if(Linea.trim().startsWith("ELSE") && IFdetectado == false){
                                  EscribirError("\tERROR 040: Comando no válido fuera de IF [ELSE].\n", ArchivoErrores); 
                              }
                              if(Linea.contains("ELSE")){
                                  boolean ELSEaceptado = true;
                                  if(IFComandoDetectado == false){
                                      EscribirError("\tERROR 041: No se ha detectado comando entre IF y ELSE [ELSE].\n", ArchivoErrores); 
                                  }
                                  if(ELSETieneMasTexto(Linea.trim())){
                                      EscribirError("\tERROR 042: Comando no debe tener más texto en la línea [ELSE].\n", ArchivoErrores); 
                                      ELSEaceptado = false;
                                  }
                                  if(ELSEaceptado == true){
                                      ELSEdetectado = true;
                                  }
                              }
                              if(ELSEdetectado == true && !Linea.contains("ELSE") && !Linea.isBlank()){
                                  ELSEComandoDetectado = true;
                              }
                              if(Linea.replaceAll(" ", "").equals("END;")){
                                  boolean ENDIFaceptado = true;
                                  if(IFComandoDetectado == false){
                                      EscribirError("\tERROR 043: No se ha detectado comando entre IF y END; [END;].\n", ArchivoErrores); 
                                  }
                                  if( ELSEdetectado == true && ELSEComandoDetectado == false){
                                      EscribirError("\tERROR 044: No se ha detectado comando entre ELSE y END; [END;].\n", ArchivoErrores);
                                  }
                                  if(ENDIFdetectado == true){
                                      EscribirError("\tERROR 045: Comando no se debe invocar más de una vez [END;].\n", ArchivoErrores); 
                                      ENDIFaceptado = false;
                                  }
                                  if(IFdetectado == false){
                                      EscribirError("\tERROR 046: Comando no se debe invocar fuera de IF [END;].\n", ArchivoErrores); 
                                      ENDIFaceptado = false;
                                  }
                                  if(ENDIFaceptado == true){
                                    ENDIFdetectado = true;
                                    IFdetectado = false;
                                }
                              }
                              
                              //Manejo de END:
                              if (ENDdetectado == true && Linea.startsWith("END")){
                                  EscribirError("\tERROR 047: Comando no debe invocarse más de una vez [END].\n", ArchivoErrores); 
                              }
                              if (!Linea.isBlank() && ENDdetectado == true){
                                  EscribirError("\tERROR 048: Comando no válido luego de END [" + Linea + "].\n", ArchivoErrores); 
                              }
                              if (Linea.trim().startsWith("END")) {
                                NombreEnd = Linea.replaceAll("END|\\.|\\;| ", "");
                                
                                if (NombreEnd.equals(NombreModulo) && Linea.trim().endsWith(".")) {
                                  ENDdetectado = true;
                                } 
                                if (!NombreEnd.equals(NombreModulo) && !NombreEnd.equals("")) {
                                  EscribirError("\tERROR 049: Identificador usado en END no coincide con el de MODULE [" + NombreEnd + "].\n", ArchivoErrores);
                                }
                                if(Linea.trim().equals("END.")){
                                    EscribirError("\tERROR 050: Comando no posee identificador [END].\n", ArchivoErrores); 
                                }
                              } 
                              if (Linea.trim().startsWith("END") && !Linea.trim().endsWith(".") && !Linea.trim().endsWith(";")){
                                  EscribirError("\tERROR 051: Comando debe terminar con punto y coma o un punto [END].\n", ArchivoErrores); 
                              }
                              if (Linea.trim().startsWith("END") && Linea.contains(".") && Linea.contains(";")){
                                  EscribirError("\tERROR 052: Comando no puede tener punto y punto y coma [END].\n", ArchivoErrores); 
                              }
                              
                              //si se detecta texto junto a END
                              Pattern pattern = Pattern.compile("^END\\s+([a-zA-Z0-9_\\-]+)\\s*;$");
                              Matcher matcher = pattern.matcher(Linea.trim());
                              if (matcher.find() && !Linea.contains(".")){
                                  EscribirError("\tERROR 053: Comando debe terminar con punto [END].\n", ArchivoErrores); 
                              }
                              
                              AnalizarTokens(Linea, BEGINdetectado, ArchivoErrores);
                          } 
          } 
        }
        //para este punto, ya se debió haber detectado MODULE, BEGIN y END.
        if (!MODULEdetectado){
            EscribirError("\tERROR 054: El comando MODULE no ha sido detectado.\n", ArchivoErrores); 
        }
        if (!BEGINdetectado){
            EscribirError("\tERROR 055: El comando BEGIN no ha sido detectado.\n", ArchivoErrores); 
        }
        if (!ENDdetectado){
            EscribirError("\tERROR 056: El comando END no ha sido detectado.\n", ArchivoErrores); 
        }
        if (REPEATdetectado && !UNTILdetectado){
            EscribirError("\tERROR 057: El comando UNTIL no ha sido detectado en el último REPEAT.\n", ArchivoErrores); 
        }
        if (IFdetectado && !ENDIFdetectado){
            EscribirError("\tERROR 058: El comando END no ha sido detectado en el último IF.\n", ArchivoErrores); 
        }
        ArchivoErrores.close();
      } catch (FileNotFoundException ex) {
        System.out.println("Archivo no encontrado.");
      } catch (IOException ex) {
        System.out.println("Error en el archivo.");
      } 
    }
  
  //manejo de tokens:
  public void AnalizarTokens(String Linea, boolean BEGINdetectado, FileWriter ArchivoErrores) {
    StringTokenizer Segmentos = new StringTokenizer(Linea, "():=; ");
    String Variable = "";
    boolean CancelarAnalisisIdentificador = false;
    boolean SaltarLinea = false;
    
    while (Segmentos.hasMoreTokens()) {
      String token = Segmentos.nextToken();
      try {
        if (!EsComandoBienEscrito(token.trim()) && !IdentificadorExiste(token) && !Linea.trim().startsWith(token)){
            EscribirError("\tERROR 059: Comando no válido de LIBELULA ni de MODULA2 [" + token + "].\n", ArchivoErrores);
            CancelarAnalisisIdentificador = true;
        }
           
        for (TablaSimbolos.Tipos ComparaTokens : TablaSimbolos.Tipos.values()) {
          boolean Encontrado = false;
          
          if (token.matches(ComparaTokens.Patron) && !SaltarLinea) {
            boolean IdentificadorDetectado;
            
            switch (ComparaTokens) {
              case ReservadaLIBELULA:
                if ((token.contains("WriteLn") || token.contains("RETURN")) && !Linea.trim().endsWith(";")){
                    EscribirError("\tERROR 060: Comando debe terminar con punto y coma [" + token + "].\n", ArchivoErrores); 
                }
                
                //manejo de los comandos Read, ReadInt, ReadReal, Write, WriteInt, WriteReal y WriteString:
                if ((token.equals("Read") || token.equals("ReadInt") || token.equals("ReadReal") || token.equals("Write") || token.equals("WriteInt") || token.equals("WriteReal") || token.equals("WriteString"))) {
                  String[] Partes;
                  
                  if (!Linea.trim().endsWith(";")){
                      EscribirError("\tERROR 061: Comando debe terminar con punto y coma [" + token + "].\n", ArchivoErrores); 
                  }
                  if (!Linea.contains("(") || !Linea.contains(")")){
                      EscribirError("\tERROR 062: Comando debe poseer ambos paréntesis [" + token + "].\n", ArchivoErrores); 
                  }
                  
                  //detectar qué hay entre paréntesis:
                  Pattern pattern = Pattern.compile("\\((.*?)\\)");
                  Matcher matcher = pattern.matcher(Linea.trim());
                  if (matcher.find()){
                      Variable = matcher.group(1);
                  }
                     
                  if (Variable.equals("") && Linea.contains("(") && Linea.contains(")")){
                      EscribirError("\tERROR 063: No se ha escrito una variable entre los paréntesis [" + token + "].\n", ArchivoErrores); 
                  }
                    
                  //revisión de la compatibilidad entre las variables y los comandos:
                  switch (token) {
                    case "Read":
                      if (!GetTipoVariable(Variable.trim()).equals("CHAR") && !GetTipoVariable(Variable.trim()).equals("")){
                          EscribirError("\tERROR 064: La variable es tipo " + GetTipoVariable(Variable.trim()) + " y debe ser tipo CHAR [" + Variable.trim() + "].\n", ArchivoErrores); 
                      }
                      break;
                      
                    case "ReadInt":
                      if (!GetTipoVariable(Variable.trim()).equals("INTEGER") && !GetTipoVariable(Variable.trim()).equals("")){
                          EscribirError("\tERROR 065: La variable es tipo " + GetTipoVariable(Variable.trim()) + " y debe ser tipo INTEGER [" + Variable.trim() + "].\n", ArchivoErrores); 
                      }
                      break;
                      
                    case "ReadReal":
                      if (!GetTipoVariable(Variable.trim()).equals("REAL") && !GetTipoVariable(Variable.trim()).equals("")){
                          EscribirError("\tERROR 066: La variable es tipo " + GetTipoVariable(Variable.trim()) + " y debe ser tipo REAL [" + Variable.trim() + "].\n", ArchivoErrores); 
                      }
                      break;
                      
                    case "Write":
                      if (!GetTipoVariable(Variable.trim()).equals("CHAR") && !GetTipoVariable(Variable.trim()).equals("")){
                          EscribirError("\tERROR 067: La variable es tipo " + GetTipoVariable(Variable.trim()) + " y debe ser tipo CHAR [" + Variable.trim() + "].\n", ArchivoErrores); 
                      }
                      break;
                      
                    case "WriteInt":
                      if (Variable.trim().startsWith(",") || Variable.trim().endsWith(",")){
                          EscribirError("\tERROR 068: Coma detectada fuera de la declaración [" + token + "].\n", ArchivoErrores); 
                      }
                      if (!Variable.trim().contains(",")) {
                          EscribirError("\tERROR 069: Comando debe poseer coma entre la variable y el tamaño [" + token + "].\n", ArchivoErrores);
                      }else{
                          Partes = Variable.trim().split(",");
                          if (!GetTipoVariable(Partes[0].trim()).equals("INTEGER") && !GetTipoVariable(Partes[0].trim()).equals("")){
                              EscribirError("\tERROR 070: La variable es tipo " + GetTipoVariable(Partes[0].trim()) + " y debe ser tipo INTEGER [" + Partes[0].trim() + "].\n", ArchivoErrores); 
                          }
                          if (!EsNumero(Partes[1].trim()) && !Partes[1].equals("")) {
                              EscribirError("\tERROR 071: El tamaño ingresado no es un número entero [" + Partes[1].trim() + "].\n", ArchivoErrores);
                          } 
                          if (!EsMayorAVeinte(Partes[1].trim()) && !Partes[1].equals("")){
                              EscribirError("\tERROR 072: El tamaño ingresado no es un valor entre 0 y 20 [" + Partes[1].trim() + "].\n", ArchivoErrores); 
                          }
                      } 
                      break;
                      
                    case "WriteReal":
                      if (Variable.trim().startsWith(",") || Variable.trim().endsWith(",")){
                          EscribirError("\tERROR 073: Coma detectada fuera de la declaración [" + token + "].\n", ArchivoErrores); 
                      }
                      if (!Variable.trim().contains(",")) {
                          EscribirError("\tERROR 074: Comando debe poseer coma entre la variable y el tamaño [" + token + "].\n", ArchivoErrores);
                      }else{
                          Partes = Variable.trim().split(",");
                          if (!GetTipoVariable(Partes[0].trim()).equals("REAL") && !GetTipoVariable(Partes[0].trim()).equals("")){
                              EscribirError("\tERROR 075: La variable es tipo " + GetTipoVariable(Partes[0].trim()) + " y debe ser tipo REAL [" + Partes[0].trim() + "].\n", ArchivoErrores); 
                          }
                          if (!EsNumero(Partes[1].trim()) && !Partes[1].equals("")) {
                            EscribirError("\tERROR 076: El tamaño ingresado no es un número entero [" + Partes[1].trim() + "].\n", ArchivoErrores);
                          } 
                          if (!EsMayorAVeinte(Partes[1].trim()) && !Partes[1].equals(""))
                            EscribirError("\tERROR 077: El tamaño ingresado no es un valor entre 0 y 20 [" + Partes[1].trim() + "].\n", ArchivoErrores); 
                          }
                      break;
                      
                    case "WriteString":
                      if (Variable.trim().replaceAll("'", "").length() > 60){
                          EscribirError("\tERROR 078: El texto no debe superar los 60 caracteres [" + token + "].\n", ArchivoErrores); 
                      }
                      if (!Variable.trim().startsWith("'") || !Variable.trim().endsWith("'")){
                          EscribirError("\tERROR 079: El texto debe tener comillas simples al inicio y al final [" + token + "].\n", ArchivoErrores); 
                      }
                      SaltarLinea = true;
                      break;
                  } 
                } 
                Encontrado = true;
                break;
                
              case ReservadaMODULA2:
                IdentificadorDetectado = false;
                for (String Identificador : this.Identificadores) {
                  if (Linea.contains(Identificador))
                    IdentificadorDetectado = true; 
                } 
                if (!IdentificadorDetectado && Linea.trim().startsWith(token)) {
                  ArchivoErrores.write("\tAdvertencia: comando no es soportado por esta versión\n");
                  SaltarLinea = true;
                } 
                Encontrado = true;
                break;
                
              case Numero:
                Encontrado = true;
                break;
                
              case OperadorLogico:
                Encontrado = true;
                break;
                
              case OperadorAritmetico:
                Encontrado = true;
                break;
                
              case SignoPuntuacion:
                Encontrado = true;
                break;
              
              case Identificador:
                if(token.endsWith(".") || token.contains(",") || token.contains("[") || token.contains(",") || token.contains("]") || token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/") ){
                    token=token.replaceAll("[\\.\\,\\+\\-\\*\\/\\[\\]]*", "");
                }
                //System.out.println("Identificador: "+ token);
                if(!Linea.contains("MODULE")){
                    if (token.length() > 20){
                        EscribirError("\tERROR 080: identificador supera los 20 caracteres [" + token + "].\n", ArchivoErrores); 
                    }
                    if(ContieneCaracteresEspeciales(token) && !EsNumero(token)){
                        EscribirError("\tERROR 081: identificador contiene caracteres especiales [" + token + "].\n", ArchivoErrores); 
                    }
                    if (EmpiezaConNumero(token) && !EsNumero(token)) {
                      EscribirError("\tERROR 082: El identificador comienza con un número [" + token + "].\n", ArchivoErrores);
                    }
                    if(!IdentificadorExiste(token) && !EsNumero(token) && !EsComandoDeLIBELULA(token) && !EsComandoDeMODULA2(token)){
                        if(BEGINdetectado == true && !Linea.contains("END") && CancelarAnalisisIdentificador == false && !Linea.trim().startsWith(token)){
                            EscribirError("\tERROR 083: El identificador no existe [" + token + "].\n", ArchivoErrores);
                        }
                    }
                }
                
                Encontrado = true;
                break;
            } 
          } 
          if (Encontrado == true)
            break; 
        } 
      } catch (IOException ex) {
        System.out.println("Archivo no encontrado.");
      } 
    } 
  }
  
  private void AnalizarDeclaracionDeVariables(String Linea, FileWriter ArchivoErrores) {
    if (!Linea.trim().endsWith(";") && !Linea.contains("BEGIN")){
        EscribirError("\tERROR 084: Comando debe terminar con punto y coma [" + Linea + "].\n", ArchivoErrores); 
    }
    if (!Linea.trim().contains(":")) {
        EscribirError("\tERROR 085: Declaración de variables debe poseer dos puntos [" + Linea + "].\n", ArchivoErrores);
    } else 
    if (Linea.trim().endsWith(";")) {
        String[] Partes = Linea.trim().split(":");
        String Tipo = Partes[1].replaceAll(";", "");
      
        if (Partes[0].startsWith(",") || Partes[0].endsWith(",")) {
          EscribirError("\tERROR 086: Coma detectada fuera de la declaración [" + Partes[0] + "].\n", ArchivoErrores);
        } else if (!TablaSimbolos.Tipos.TiposVariables.Patron.contains(Tipo.trim())) {
            EscribirError("\tERROR 087: El tipo de variable declarada no existe [" + Tipo + "].\n", ArchivoErrores);
        } else {
          boolean ColocarValores = true;
          if (Partes[0].contains(",")) {
            boolean AnalizarDatos = true;
            if (ContieneComasRepetidas(Partes[0].trim())) {
              EscribirError("\tERROR 088: Hay comas repetidas en la inicialización de variables [" + Partes[0] + "].\n", ArchivoErrores);
              AnalizarDatos = false;
            } 
            if (Partes[0].trim().startsWith(",") || Partes[0].trim().endsWith(",")) {
              EscribirError("\tERROR 089: Coma fuera de la inicialización detectada [" + Partes[0] + "].\n", ArchivoErrores);
              AnalizarDatos = false;
            } 
            if (AnalizarDatos == true) {
              String[] Separar = Partes[0].split(",");
              for (String Valores : Separar) {
                if (EmpiezaConNumero(Valores.trim())) {
                  EscribirError("\tERROR 090: El identificador comienza con un número [" + Valores.trim() + "].\n", ArchivoErrores);
                  ColocarValores = false;
                } 
                if (IdentificadorExiste(Valores.trim())) {
                  EscribirError("\tERROR 091: El identificador ya existe [" + Valores.trim() + "].\n", ArchivoErrores);
                  ColocarValores = false;
                } 
                if (EsComandoDeLIBELULA(Valores.trim())) {
                  EscribirError("\tERROR 092: El identificador es un comando de LIBELULA [" + Valores.trim() + "].\n", ArchivoErrores);
                  ColocarValores = false;
                } 
                if (EsComandoDeMODULA2(Valores.trim())) {
                  EscribirError("\tERROR 093: El identificador es un comando de MODULA2 [" + Valores.trim() + "].\n", ArchivoErrores);
                  ColocarValores = false;
                } 
                if (ColocarValores == true) {
                  //Para este punto, los identificadores son correctos.
                  Identificadores.add(Valores.trim());
                  Tipos.add(Tipo.trim());
                } 
              } 
            } 
          } else {
              if (EmpiezaConNumero(Partes[0].trim())) {
                EscribirError("\tERROR 094: El identificador comienza con un número [" + Partes[0].trim() + "].\n", ArchivoErrores);
                ColocarValores = false;
              } 
              if (IdentificadorExiste(Partes[0].trim())) {
                EscribirError("\tERROR 095: El identificador ya existe [" + Partes[0].trim() + "].\n", ArchivoErrores);
                ColocarValores = false;
              } 
              if (EsComandoDeLIBELULA(Partes[0].trim())) {
                EscribirError("\tERROR 096: El identificador es un comando de LIBELULA [" + Partes[0].trim() + "].\n", ArchivoErrores);
                ColocarValores = false;
              } 
              if (EsComandoDeMODULA2(Partes[0].trim())) {
                EscribirError("\tERROR 097: El identificador es un comando de MODULA2 [" + Partes[0].trim() + "].\n", ArchivoErrores);
                ColocarValores = false;
              } 
              if (ColocarValores == true) {
                //Para este punto, los identificadores son correctos.
                Identificadores.add(Partes[0].trim());
                Tipos.add(Tipo.trim());
              } 
            } 
        } 
    } 
  }
  
  private void AnalizarAsignacionDeVariables(String Linea, boolean BEGINdetectado, String PrimerValor, FileWriter ArchivoErrores){
      String[] Partes = Linea.split(":=");  
      Partes[0] = Partes[0].replaceAll(" ", "");    
      
      if (Linea.contains(":=") && !BEGINdetectado){
          EscribirError("\tERROR 098: Asignación de variables declarada antes de BEGIN [" + Linea + "].\n", ArchivoErrores); 
      }
      if(IdentificadorExiste(PrimerValor) && !Linea.contains(":=") && BEGINdetectado == true){
          EscribirError("\tERROR 099: Asignación de variables debe tener := [" + Linea.trim() + "].\n", ArchivoErrores);  
      }
      if (Linea.contains(":=") && BEGINdetectado == true) {
        if (!Linea.trim().endsWith(";")){
           EscribirError("\tERROR 100: Comando debe terminar con punto y coma [" + Linea + "].\n", ArchivoErrores);  
        }
        if(!VerificarParentesis(Linea)){
            EscribirError("\tERROR 101: Comando no ha abierto y cerrado todos los paréntesis [" + Linea.trim() + "].\n", ArchivoErrores); 
        }
        if(!IdentificadorExiste(Partes[0])){
            EscribirError("\tERROR 102: Identificador no existe [" + Partes[0] + "].\n", ArchivoErrores); 
        }
        Pattern pattern = Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\b|\\b[a-zA-Z0-9&$]+\\b");
        Matcher matcher1 = pattern.matcher(Linea);

        List<String> componentes = new ArrayList<>();
        while (matcher1.find()) {
            componentes.add(matcher1.group());
            if(EsNumero(matcher1.group()) && (matcher1.group().startsWith(".") || matcher1.group().endsWith(".")) || matcher1.group().startsWith(",") || matcher1.group().endsWith(",")){
                EscribirError("\tERROR 103: separador decimal es innecesario [" + matcher1.group() + "].\n", ArchivoErrores); 
            }
            if(matcher1.group().contains(",")){
                EscribirError("\tERROR 104: Número debe poseer punto [" + matcher1.group() + "].\n", ArchivoErrores); 
            }
            if(IdentificadorExiste(matcher1.group()) && IdentificadorExiste(Partes[0].replaceAll(" ", ""))){
                if(!GetTipoVariable(Partes[0].replaceAll(" ", "")).equals(GetTipoVariable(matcher1.group()))){
                    EscribirError("\tERROR 105: Variable " +Partes[0].trim()+ " es tipo " +GetTipoVariable(Partes[0].trim())+ " y variable " +matcher1.group()+ " es tipo " +GetTipoVariable(matcher1.group())+ " [" + matcher1.group() + "].\n", ArchivoErrores); 
                }
            }
            if(IdentificadorExiste(Partes[0])){
                if(GetTipoVariable(Partes[0]).equals("CHAR") && EsNumero(matcher1.group())){
                    EscribirError("\tERROR 106: variable "+Partes[0]+ " es tipo CHAR, por lo que no puede aceptar números [" + matcher1.group() + "].\n", ArchivoErrores); 
                }
                if(GetTipoVariable(Partes[0]).equals("INTEGER") && (matcher1.group().contains(",") || matcher1.group().contains("."))){
                    EscribirError("\tERROR 107: variable "+Partes[0]+ " es tipo INTEGER, por lo que no puede aceptar números con decimales [" + matcher1.group() + "].\n", ArchivoErrores); 
                }
                if(GetTipoVariable(Partes[0]).equals("REAL") && EsNumero(matcher1.group()) && !matcher1.group().matches(".*[.,].*")){
                    EscribirError("\tERROR 108: variable "+Partes[0]+ " es tipo REAL, por lo que no puede aceptar números sin decimales [" + matcher1.group() + "].\n", ArchivoErrores); 
                }
            }
        }
      } 
  }
  
  private boolean BeginTieneMasTexto(String Linea){
      return Linea.matches("^BEGIN\\s.*$");
  }
  
  private boolean REPEATTieneMasTexto(String Linea){
      return Linea.matches("^REPEAT\\s.*$");
  }
  
  private boolean IFTieneMasTexto(String Linea){
      return Linea.matches("^IF\\s.*$");
  }
  
  private boolean ELSETieneMasTexto(String Linea){
      return Linea.matches("^ELSE\\s.*$");
  }
  
  private boolean EsComandoBienEscrito(String Valor) {
    boolean Confirmar = true;
    for (String Valores : ValoresLIBELULA) {
      if (!Valor.equals(Valores) && Valor.toLowerCase().equals(Valores.toLowerCase()))
        Confirmar = false; 
    } 
    for (String Valores : ValoresMODULA2) {
      if (!Valor.equals(Valores) && Valor.toLowerCase().equals(Valores.toLowerCase()))
        Confirmar = false; 
    } 
    return Confirmar;
  }
  
  private boolean EsNumero(String Valor) {
    return Valor.matches("-?\\d+(\\.\\d+|,\\d+)?");
  }
  
  private boolean EsMayorAVeinte(String Valor) {
    try {
      int num = Integer.parseInt(Valor);
      return (num >= 0 && num <= 20 && Valor.equals(Integer.toString(num)));
    } catch (NumberFormatException e) {
      return false;
    } 
  }
  
  private boolean EmpiezaConNumero(String Valor) {
    Pattern pattern = Pattern.compile("^\\d");
    Matcher matcher = pattern.matcher(Valor);
    return matcher.find();
  }
  
  private boolean ContieneCaracteresEspeciales(String Valor) {
    return Valor.matches(".*[¿?$&%().,;:/\\\\_-].*");
  }
  
  public boolean ContieneComasRepetidas(String Valor) {
    Pattern pattern = Pattern.compile(",{2,}");
    Matcher matcher = pattern.matcher(Valor);
    return matcher.find();
  }
  
  private boolean EsComandoDeLIBELULA(String Valor) {
    boolean Detectado = false;
    for (String Comando : ValoresLIBELULA) {
      if (Comando.equals(Valor)){
          Detectado = true; 
      }
    } 
    return (Detectado == true);
  }
  
  private boolean EsComandoDeMODULA2(String Valor) {
    boolean Detectado = false;
    for (String Comando : ValoresMODULA2) {
      if (Comando.equals(Valor)){
          Detectado = true; 
      }
    } 
    return (Detectado == true);
  }
  
  private boolean EsTipoVariable(String Valor) {
    boolean Detectado = false;
    for (String Comando : ListaVariables) {
      if (Comando.equals(Valor)){
          Detectado = true; 
      }
    } 
    return (Detectado == true);
  }
  
  private boolean IdentificadorExiste(String Valor) {
    boolean IdentDetectado = false;
    for (String Identificador : Identificadores) {
      if (Identificador.equals(Valor.trim())){
          IdentDetectado = true; 
      }
    } 
    return IdentDetectado;
  }
  
  private String GetTipoVariable(String Valor) {
    String TipoVariable = "";
    for (int i = 0; i < Identificadores.size(); i++) {
      if ((Identificadores.get(i)).trim().equals(Valor.trim())){
          TipoVariable = Tipos.get(i).trim(); 
      }
    } 
    return TipoVariable;
  }
  
  //Se usó una pila para verificar los paréntesis. 
  private boolean VerificarParentesis(String linea) {
    Stack<Character> pila = new Stack<>();

    for (int i = 0; i < linea.length(); i++) {
        char c = linea.charAt(i);

        if (c == '(' ) { 
            pila.push(c);   //añadir "("
        } else if (c == ')' ) { 
            if (pila.isEmpty()) {
                return false;   //error si se añaden dos "(" seguidos
            }
            char ultimaApertura = pila.pop();
            if (c == ')' && ultimaApertura != '(') {
                return false;  
            }
        }
    }

    return pila.isEmpty(); //error si no se cerró
  }
  
  //total de veces que se invoca este método al día de la entrega del proyecto: 108 veces.
  private void EscribirError(String Error, FileWriter ArchivoErrores) {
    try {
      ArchivoErrores.write(Error);
      ErrorDetectado = true;
    } catch (IOException ex) {
      System.out.println("Archivo no encontrado.");
    } 
  }
  
  
  public boolean getErrorDetectado() {
    return this.ErrorDetectado;
  }
}
