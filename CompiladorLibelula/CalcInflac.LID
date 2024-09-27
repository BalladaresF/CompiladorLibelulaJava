MODULE CalcularInflacion;

  FROM InOut     IMPORT WriteInt, WriteString, WriteLn, Write, Read, ReadInt;
  FROM RealInOut IMPORT WriteReal, ReadReal;

VAR
  MaxAnos   : INTEGER ;
  Ano       : INTEGER ;
  respuesta : CHAR ;
  Factor1 , Factor2 , Factor3 : REAL ;

BEGIN

(*
  *****************************************************************
  * Asumiendo tasas de inflacion anual de 7%, 8%, y 10%,          *
  * encontrar el factor por el cual cualquier moneda, tales como  *
  * el franco, el dolar, la libra esterlina, el marco, el rublo,  *
  * el yen o el florin han sido devaluadas en 1, 2, ...., N anos. *
  *****************************************************************
*)

    (* Inicio del programa *)
    WriteLn ;

    (* Entrada de datos *)
    WriteString ( 'Por favor, indique la cantidad maxima de anos:' );
    WriteLn ;
    WriteLn ;

    ReadInt ( MaxAnos );

    IF ( MaxAnos <= 0 ) THEN
       RETURN;
    END;

    (* Inicializacion de variables *)
    Ano := 0;     Factor1 := 1.0;     Factor2 := 1.0;     Factor3 := 1.0;

    (* Calculos y salida de datos *)
    WriteLn;  WriteString ( '        Ano 7%           8%          10%' );  WriteLn;

    REPEAT
      Factor1 := ( Factor1 * 1.07 );
      Factor2 := ( Factor2 ) * 1.08;
      Factor3 := Factor3 * ( 1.10 );
      WriteInt  ( Ano,      3 ) ;
      WriteReal ( Factor1, 10 ) ;
      WriteReal ( Factor2, 10 ) ;
      WriteReal ( Factor3, 10 ) ;
      WriteLn;      
      WriteLn;            
      Ano := Ano + 1;
    UNTIL Ano > MaxAnos;

    WriteLn;
    WriteString ( '*** Fin de calculos ***' );
    WriteLn;
    WriteLn;

    WriteString ( 'Otra vez?' );
    Read ( respuesta );
    WriteLn;
    WriteString ( 'Lo que respondio: ' );
    Write ( respuesta );
    WriteLn;
    WriteLn;

    WriteString ( '*** Fin del programa ***' );
    WriteLn;
    WriteString ( '*** Hasta luego ***' );
    WriteLn;

END CalcularInflacion.

(* fin del programa *)
