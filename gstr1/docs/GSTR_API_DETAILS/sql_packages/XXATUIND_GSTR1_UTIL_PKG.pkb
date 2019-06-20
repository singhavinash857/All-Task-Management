CREATE OR REPLACE PACKAGE BODY XXWEB.XXATUIND_GSTR1_JSON_UTIL_PKG
IS

--Global Variables
gv_access_token  VARCHAR2(32000);
gv_token         VARCHAR2(32000);

--/************************************************************************************************
--*Procedure set_ora_wallet is used to open oracle wallet to give access
--*************************************************************************************************/
PROCEDURE set_ora_wallet(p_wallet_path IN VARCHAR2
                       , p_wallet_pwd  IN VARCHAR2
					   , x_error_message OUT VARCHAR2)
IS
BEGIN

    UTL_HTTP.set_wallet(p_wallet_path,p_wallet_pwd);

    x_error_message := null;
EXCEPTION
WHEN OTHERS THEN
	x_error_message := 'Error in set_ora_wallet proc ::: '||sqlerrm;
END set_ora_wallet;

--/************************************************************************************************
--*Function gstr1_save_proc is used to save json data into custom tables
--*************************************************************************************************/
FUNCTION gstr1_save_proc(   p_wallet_path     IN   VARCHAR2,
						    p_wallet_pwd      IN   VARCHAR2,
						    p_json_data       IN VARCHAR2,                           
                            p_period          IN VARCHAR2,                        
                            p_batch_id        IN NUMBER,                          
							p_token           IN VARCHAR2,                        
							p_save_url        IN VARCHAR2,                        
							p_ocp_key         IN VARCHAR2,                        
							p_gstinno         IN VARCHAR2,                        
							p_reference_no    IN VARCHAR2,                        
							p_connection      OUT VARCHAR2,                       
							p_error_message   OUT VARCHAR2) 
  RETURN XMLTYPE        
   IS                                                                             
    v_request     UTL_HTTP.req;                                                   
    v_response    UTL_HTTP.resp;                                                  
    ln_token_len  NUMBER;                                                         
    v_text        VARCHAR2(32000);
    responsebody  clob:=null;
	resplength    binary_integer;
	lv_error_message  VARCHAR2(4000);
    buffer2       varchar2(32767);
    eob           BOOLEAN := false; -- END-OF-BODY flag (Boolean)
    resp          XMLTYPE;

BEGIN
 --Open Wallet to send allow integration
	 set_ora_wallet(p_wallet_path, p_wallet_pwd, lv_error_message);
	 
	  IF lv_error_message IS NOT NULL
	 THEN
		p_error_message := lv_error_message;

		RETURN NULL;

	 END IF;


   v_text:= p_json_data;


    v_request := UTL_HTTP.begin_request(p_save_url, 'POST');
    UTL_HTTP.set_header (v_request,
                        'Ocp-Apim-Subscription-Key',
                        p_ocp_key
                       );


   UTL_HTTP.set_header (v_request,
                        'GSTINNO ',
                        p_gstinno
                       );


   UTL_HTTP.set_header (v_request,
                        'SOURCE_TYPE',
                        'ERP'
                       );


   UTL_HTTP.set_header (v_request,
                        'REFERENCE_NO   ',
                        p_reference_no
                       );




   UTL_HTTP.set_header (v_request,
                      'Content-Type',
                      'application/json'
                       );



   UTL_HTTP.set_header(v_request,
                       'Content-Length',
                       LENGTH(v_text));


    UTL_HTTP.set_header(v_request,
                       'OUTPUTTYPE',
                       'XML' );





    UTL_HTTP.write_text(v_request, v_text);


    v_response := UTL_HTTP.get_response(v_request);

	p_connection := v_response.status_code;

    dbms_lob.createtemporary(responsebody, true);

	while not(eob)
    loop
	  begin
		utl_http.read_text(v_response, buffer2, 32767); -- buffer = VARCHAR2(32767)
		if buffer2 is not null and length(buffer2)>0 then
		   dbms_lob.writeappend(responsebody, length(buffer2), buffer2);
		end if;
	 exception
		when UTL_HTTP.END_OF_BODY
		then eob := true;
	 end;
   end loop;


   resp := XMLType.createXML(responsebody);

   IF (instr(resp.getStringVal(), 'ERROR:') > 0)
   THEN
	  raise_application_error (-20999, 'Call Webservice: Failed! ');
   END IF;

   resplength := dbms_lob.getlength(responsebody);

   dbms_lob.freetemporary(responsebody);


    UTL_HTTP.end_response(v_response);

	RETURN resp;

	EXCEPTION
    WHEN UTL_HTTP.TOO_MANY_REQUESTS THEN
       p_error_message := 'Too Many Requests';
       RETURN NULL;
	  WHEN UTL_HTTP.REQUEST_FAILED
	  THEN p_error_message := 'Request_Failed: ' || UTL_HTTP.GET_DETAILED_SQLERRM;
		   RETURN null;
	  WHEN UTL_HTTP.HTTP_SERVER_ERROR
	  THEN p_error_message := 'Http_Server_Error: ' || UTL_HTTP.GET_DETAILED_SQLERRM;
		   RETURN null;
	  WHEN UTL_HTTP.HTTP_CLIENT_ERROR
	  THEN p_error_message := 'Http_Client_Error: ' || UTL_HTTP.GET_DETAILED_SQLERRM;
		   RETURN null;
	  WHEN OTHERS
	  THEN p_error_message := ''||sqlerrm;
		   RETURN null;
  END gstr1_save_proc;

END XXATUIND_GSTR1_JSON_UTIL_PKG;
/