/*
 * 
 */
package Clases;

/**
 * 
 */
public class TablaSimbolos {
    enum Tipos{
        //Algunas palabras reservadas de LIBELULA también están en MODULA2. Estas palabras solo se han colocado en LIBELULA.
        ReservadaLIBELULA("(BEGIN|CHAR|ELSE|END|IF|INTEGER|MODULE|Read|ReadInt|ReadReal|REAL|REPEAT|RETURN|THEN|UNTIL|VAR|Write|WriteInt|WriteLn|WriteReal|WriteString)"),
        ReservadaMODULA2("(ABS|ABSTRACT|AND|ARRAY|AS|BITSET|BOOLEAN|BY|CAP|CARDINAL|CASE|CLASS|CMPLX|COMPLEX|CONST|DEC|DEFINITION|DISPOSE|DIV|DO|ELSIF|EXCEPT|EXCL|EXIT|EXPORT|FALSE|FINALLY|FLOAT|FOR|FORWARD|FROM|GENERIC|GUARD|HALT|HIGH|IM|IMPLEMENTATION|IMPORT|IN|INC|INCL|INHERIT|INT|INTERRUPTIBLE|LENGTH|LFLOAT|LONGCOMPLEX|LONGREAL|LOOP|MAX|MIN|MOD|NEW|NIL|NOT|ODD|OF|OR|ORD|OVERRIDE|PACKEDSET|POINTER|PROC|PROCEDURE|PROTECTION|QUALIFIED|RE|READONLY|RECORD|REM|RETRY|REVEAL|SET|SIZE|TO|TRACED|TRUE|TRUNC|TYPE|UNINTERRUPTIBLE|UNSAFEGUARDED|WHILE|WITH)"),
        Numero("[0-9]*|[0.0-9.9]*"),
        //Identificador("[A-Za-z]*[0-9]*"),
        //Identificador("[\\p{L}\\p{N}¿?\\$&%/_\\-.]*"),
        //Identificador("[\\p{L}\\p{N}¿?\\$&%/_\\-.()\\[\\],+-\\/(\\*)]+"),
        Identificador("[\\p{L}\\p{N}¿?\\$&%/_\\-.()\\[\\],+-\\/(\\*)]*"),
        OperadorLogico("(=|>|<|>=|<=|!=)"),
        OperadorAritmetico("(\\+|\\-|\\*|\\/)"),
        SignoPuntuacion("(,|;|.|:)"),
        TiposVariables("(CHAR|INTEGER|REAL)");
        
        public final String Patron;
        Tipos(String s){
            this.Patron = s;
        }
    }
}

//las palabras reservadas se han obtenido de los anexos 1 (págs. 36-38) y 2 (pág. 39) del enunciado del proyecto.