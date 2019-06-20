CREATE OR REPLACE PACKAGE BODY APPS.XXATUIND_GSTR1_JSON_PKG
IS

--Global Variables
gv_access_token  VARCHAR2(32000);
gv_token         VARCHAR2(32000);

/*******************************************************************************************************************
*
* Oracle Applications : R12
* I --> Initial
* E --> Enhancement
* R --> Requirement
* B --> Bug
********************************************************************************************************************
--$Header: XXATUIND_GSTR1_JSON_PKG.pkb 0.0.0.0  16-Feb-2018  Rohit Prasad  $
/*******************************************************************************************************************
* Type                :  Package Body
* Name                :  XXATUIND_GSTR1_JSON_PKG
* Script Name         :  XXATUIND_GSTR1_JSON_PKG.pkb
* Procedures          :
*
* Functions           :
* Purpose             : Package to Generate GST1 Data like Json format and process to wepsol
*
* Company             : Transform Edge
* Created By          : Rohit Prasad
* Created Date        : 16-Feb-2018
* Last Reviewed By    :
* Last Reviewed Date  :
********************************************************************************************************************
* <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
* Date        By               Script   MKS        By                Date        Type Details
* ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
* 16-Feb-2018   Rohit               Draft1                                           I      Initial Version
* 25-March-2018 Rohit               Draft1.1                                         II     Modified Version
********************************************************************************************************************/
   PROCEDURE process_jason_format_proc(p_ret_message   OUT VARCHAR2,
                                       p_ret_code      OUT NUMBER,
                                       p_period        IN VARCHAR2,
                                       p_batch_id      IN NUMBER,
                                       p_first_party_prim_reg_num IN VARCHAR2
                                       )
   AS
    /********************************************************************************************************************
    * Type                : Procedure
    * Name                : process_jason_format_proc
    * Input Parameters    : 1.p_period
    *                     : 2.p_batch_id
    *                      :    3.p_first_party_prim_reg_num
    * Purpose             : Generate GST1 Data like Json format
    * Company             : Transform Edge
    * Created By          : Rohit Prasad
    * Created Date        : 16-Feb-2018
    * Modified Date       : 25-March-2018
    * Last Reviewed By    :
    * Last Reviewed Date  :
    ********************************************************************************************************************
    * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
    * Date        By               Script   MKS        By                Date         Type     Details
    * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
    * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
    ********************************************************************************************************************/

   CURSOR main_cur
   IS
    SELECT first_party_primary_reg_num    gstn,
           period_name                    fp1,
           NVL(gross_turnover,0)          gt,
           NVL(current_gross_turnover,0)  cur_gt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
     GROUP BY first_party_primary_reg_num,
              period_name,
              gross_turnover,
              current_gross_turnover
              ;

   CURSOR ctin_b2b_extract(p_gstn   IN VARCHAR2,
                           p_gt     IN NUMBER,
                           p_cur_gt IN NUMBER
             )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           third_party_reg_num ctin
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'B2B'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
  GROUP BY   first_party_primary_reg_num,
             period_name,
             gross_turnover,
             current_gross_turnover,
             third_party_reg_num
           ;

   CURSOR b2b_inv(p_gstn                IN VARCHAR2,
                  p_gt                  IN NUMBER,
                  p_cur_gt              IN NUMBER,
                  p_third_party_reg_num IN VARCHAR2
                 )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           third_party_reg_num ctin,
           flag ,
           TAX_INVOICE_NUMBER inum,
           REPLACE(TAX_INVOICE_NUMBER,'ATU/TN','ATUTN') inum1,
           TO_CHAR(tax_invoice_date,'DD/MM/YYYY') idt,
           tax_invoice_value val,
           state  pos,
           rev_charge rchrg,
           ecom_operator etin,
           SUBSTR(inv_type,0,1)  inv_typ
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'B2B'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
       AND NVL(third_party_reg_num,'X') = NVL(p_third_party_reg_num,'X')
  GROUP BY first_party_primary_reg_num ,
           period_name,
           gross_turnover,
           current_gross_turnover,
           third_party_reg_num,
           flag,
           TAX_INVOICE_NUMBER,
           tax_invoice_date,
           tax_invoice_value,
           state,
           rev_charge,
           ecom_operator,
           inv_type
      ;

   CURSOR b2b_item(p_gstn                IN VARCHAR2,
                   p_gt                  IN NUMBER,
                   p_cur_gt              IN NUMBER,
                   p_third_party_reg_num IN VARCHAR2,
                   p_invoice             IN VARCHAR2
                  )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           third_party_reg_num ctin,
           flag ,
           TAX_INVOICE_NUMBER inum,
           REPLACE(TAX_INVOICE_NUMBER,'ATU/TN','ATUTN') inum1,
           TO_CHAR(tax_invoice_date,'DD/MM/YYYY') idt,
           tax_invoice_value val,
           state  pos,
           rev_charge rchrg,
           ecom_operator etin,
           SUBSTR(inv_type,0,1)  inv_typ,
           serial_no num,
           tax_rate  rt,
           NVL(taxable_amt,0) txval,
           NVL(IGST,0) iamt,
           NVL(CGST,0) camt,
           NVL(SGST,0) samt,
           NVL(CESS,0) csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'B2B'
       AND first_party_primary_reg_num = p_gstn
       AND TAX_INVOICE_NUMBER = p_invoice
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
       AND NVL(third_party_reg_num,'X') = NVL(p_third_party_reg_num,'X')
      ;


   CURSOR nil(p_gstn   IN VARCHAR2,
              p_gt     IN NUMBER,
              p_cur_gt IN NUMBER
             )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           nil_amount nil_amt,
           expt_amount expt_amt,
           ngsup_amount ngsup_amt,
           supp_type sply_ty
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'NIL'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;

   CURSOR export(p_gstn   IN VARCHAR,
                 p_gt     IN NUMBER,
                 p_cur_gt IN NUMBER
                )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           FLAG,
           EXPORT_TYPE,
           REPLACE(TAX_INVOICE_NUMBER,'ATU/TN','ATUTN') inum,
           TO_CHAR(tax_invoice_date,'DD/MM/YYYY') idt,
           TAX_INVOICE_VALUE val,
           SHIPPABLE_BILL_NO sbnum,
           SHIPPABLE_BILL_DATE sbdt,
           NVL(TAXABLE_AMT,0)  txval,
           TAX_RATE  rt,
           NVL(IGST,0)  iamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'EXP'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;

   CURSOR Adv_tax (p_gstn   IN VARCHAR2,
                   p_gt     IN NUMBER,
                   p_cur_gt IN NUMBER
             )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           flag ,
           NVL(state,0)POS,
           supp_type SPLY_TY,
           NVL(tax_rate,0) rt,
           adv_receipt_amt AD_AMT,
           NVL(igst,0) iamt,
           NVL(cgst,0) camt,
           NVL(sgst,0) samt,
           NVL(cess,0) csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'AT'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)       = NVL(p_cur_gt,0)
      ;

   CURSOR txpd(p_gstn   IN VARCHAR2,
               p_gt     IN NUMBER,
               p_cur_gt IN NUMBER
            )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           flag ,
           NVL(state,0)POS,
           supp_type SPLY_TY,
           NVL(tax_rate,0) RT,
           NVL(adv_receipt_amt,0) AD_AMT,
           NVL(igst,0) iamt,
           NVL(cgst,0) camt,
           NVL(sgst,0) samt,
           NVL(cess,0) csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'TXPD'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;

   CURSOR hsn(p_gstn   IN VARCHAR2,
              p_gt     IN NUMBER,
              p_cur_gt IN NUMBER
             )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           serial_no num,
           hsn_code  hsn_sc,
           hsn_code  desc1,
           ATTRIBUTE9       uqc,

           qty       qty,
           tax_invoice_value val,
           NVL(taxable_amt,0) txval,
           NVL(igst,0) iamt,
           NVL(cgst,0) camt,
           NVL(sgst,0) samt,
           NVL(cess,0) csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'HSN'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;

   CURSOR cdnr_main_cur(p_gstn   IN VARCHAR2,
                        p_gt     IN NUMBER,
                        p_cur_gt IN NUMBER
                        )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           third_party_reg_num ctin
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'CDNR'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
  GROUP BY first_party_primary_reg_num,
           period_name,
           gross_turnover,
           current_gross_turnover,
           third_party_reg_num
      ;

   CURSOR cdnr_child_cur(p_gstn   IN VARCHAR2,
                         p_gt     IN NUMBER,
                         p_cur_gt IN NUMBER,
                         p_third_party_reg_num IN VARCHAR2
             )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           third_party_reg_num ctin,
           FLAG,
           EVENT_CLASS_CODE ntty,
           TRX_NUMBER nt_num,
           TRX_DATE nt_dt,
           RSN,
           P_GST,
           NVL(REPLACE(TAX_INVOICE_NUMBER,'ATU/TN','ATUTN'),0) inum,
           NVL(TO_CHAR(tax_invoice_date,'DD/MM/YYYY'),0) idt,
           nvl(TAX_INVOICE_VALUE,0) val,
           SERIAL_NO  num,
           TAX_RATE   rt,
           NVL(TAXABLE_AMT,0)  txval,
           NVL(igst,0) iamt,
           NVL(cgst,0) camt,
           NVL(sgst,0) samt,
           NVL(cess,0) csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'CDNR'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
       AND NVL(third_party_reg_num,'X') = NVL(p_third_party_reg_num,'X')
      ;

   CURSOR cdnur(p_gstn   IN VARCHAR2,
                p_gt     IN NUMBER,
                p_cur_gt IN NUMBER
             )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           third_party_reg_num ctin,
           FLAG,
           EXPORT_TYPE typ,
           EVENT_CLASS_CODE ntty,
           TRX_NUMBER nt_num,
           TRX_DATE nt_dt,
           RSN,
           P_GST,
           REPLACE(TAX_INVOICE_NUMBER,'ATU/TN','ATUTN') inum,
           TO_CHAR(tax_invoice_date,'DD/MM/YYYY')   idt,
           TAX_INVOICE_VALUE  val,
           SERIAL_NO  num,
           TAX_RATE   rt,
           NVL(TAXABLE_AMT,0)  txval,
           NVL(igst,0) iamt,
           NVL(cgst,0) camt,
           NVL(sgst,0) samt,
           NVL(cess,0) csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'CDNUR'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;

   CURSOR doc(p_gstn   IN VARCHAR2
              )
   IS
    SELECT flag,
           attribute1  doc_num,
           attribute2  doc_typ,
           serial_no   num,
           REPLACE(attribute3,'ATU/TN','ATUTN') from1,
           REPLACE(attribute4,'ATU/TN','ATUTN') to1,
           attribute5  totnum,
           attribute6  cancel,
           attribute7  net_issue
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'DOC'
       AND first_party_primary_reg_num = p_gstn
          ;

   CURSOR b2cl(p_gstn   IN VARCHAR2,
               p_gt     IN NUMBER,
               p_cur_gt IN NUMBER
              )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           STATE pos,
           FLAG,
           REPLACE(TAX_INVOICE_NUMBER,'ATU/TN','ATUTN') inum,
           TO_CHAR(tax_invoice_date,'DD/MM/YYYY')  idt,
           TAX_INVOICE_VALUE  val,
           ECOM_OPERATOR  etin,
           SERIAL_NO     num,
           NVL(TAX_RATE,0)     rt,
           NVL(TAXABLE_AMT,0)   txval,
           NVL(IGST,0)          iamt,
           NVL(CESS,0)          csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'B2CL'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;

      CURSOR b2cs(p_gstn   IN VARCHAR2,
                  p_gt     IN NUMBER,
                  p_cur_gt IN NUMBER
              )
   IS
    SELECT first_party_primary_reg_num gstn,
           period_name fp,
           NVL(gross_turnover,0) gt,
           NVL(current_gross_turnover,0) cur_gt,
           SUBSTR(STATE,0,2) pos,
           (CASE WHEN IGST > 0 then 'Inter' else 'Intra' end) sply_ty,
           SUBSTR(inv_type,0,1) typ,
           ECOM_OPERATOR  etin,
           TAX_RATE      rt,
           NVL(TAXABLE_AMT,0)   txval,
           NVL(IGST,0)          iamt,
           NVL(CGST,0)          camt,
           NVL(SGST,0)          samt,
           NVL(CESS,0)          csamt
      FROM XXATUIND_GSTR1_MAIN_TAB
     WHERE PERIOD_NAME    = g_period_no
       AND batch_id       = p_batch_id
       AND process_status = 'I'
       AND SECTION_CODE   = 'B2CS'
       AND first_party_primary_reg_num = p_gstn
       AND NVL(gross_turnover,0)              = NVL(p_gt,0)
       AND NVL(current_gross_turnover,0)      = NVL(p_cur_gt,0)
      ;
--Local CLOB variables

   l_chr_ctin_final_data     CLOB;
   l_chr_invb2b_final_data   CLOB;
   l_chr_itemb2b_final_data  CLOB;
   l_chr_final_data          CLOB;
   l_b2b_merge_data          CLOB;
   l_chr_nil_final_data      CLOB;



   --Local Main Headers Variables for B2B component
   l_chr_main_start         VARCHAR2(3000);
   l_chr_main_end           VARCHAR2(3000);
   l_chr_ctin_data          VARCHAR2(3000);
   l_chr_invb2b_data        VARCHAR2(3000);
   l_chr_itemb2b_data       VARCHAR2(3000);
   l_chr_b2b_start         VARCHAR2(3000);
   l_num_itemb2b            NUMBER;
   l_num_invb2b             NUMBER;
   l_num_ctin               NUMBER;
   l_chr_ctin_end           VARCHAR2(20) := '] }';
   l_chr_invb2b_end         VARCHAR2(20) := ']'|| CHR(10)||'} ';
   l_chr_b2b_end            VARCHAR2(20) := ']';
   l_chr_itemb2b_end        VARCHAR2(20) := '}';
   l_num_inv_tot_rec        NUMBER;
   l_num_ctin_tot_rec       NUMBER;
   l_num_rownum1            NUMBER;



   --nil cursor local variables
   l_char_nil_start         VARCHAR2(20) := ',"nil": {';
   l_char_nil_end           VARCHAR2(20) := '}';
   l_char_inv1_start        VARCHAR2(20) := '"inv": [';
   l_char_inv1_end          VARCHAR2(20) := ']';
   l_chr_nil_data           VARCHAR2(3000);
   l_chr_nil_map_data       CLOB;
   l_num_rownum5            NUMBER;

 --Exp cursor local variables
   l_char_exp_start    VARCHAR2(250) := ', "exp": ['||CHR(10);
   l_char_exptwo_start VARCHAR2(250) := ',{'||CHR(10)||'"exp_typ": "WOPAY",'||CHR(10)||'"inv": [';
   l_char_exptw_end    VARCHAR2(250) := CHR(10)||']'|| CHR(10)||'}';
   l_char_exptwo_end   VARCHAR2(250) := CHR(10)||']'|| CHR(10)||'}';
   l_char_exp_end      VARCHAR2(250) := ']';
   l_chr_expw_data     VARCHAR2(3000);
   l_chr_expwo_data    VARCHAR2(3000);
   l_chr_exp_map_data  CLOB;
   l_num_rownum6       NUMBER;
   l_chr_exp_final_data CLOB;

 --AT cursor local variables
   l_char_at_start    VARCHAR2(220) := ',"at": [';
   l_char_at_end      VARCHAR2(220) := ']';
   l_chr_at_data      VARCHAR2(3000);
   l_chr_at_map_data  CLOB;
   l_num_rownum7      NUMBER;
   l_chr_at_final_data  CLOB;

    --TXPD cursor local variables
   l_char_txpd_start      VARCHAR2(220) := ',"txpd": [';
   l_char_txpd_end        VARCHAR2(220) := ']';
   l_chr_txpd_data        VARCHAR2(3000);
   l_chr_txpd_map_data    CLOB;
   l_num_rownum8          NUMBER;
   l_chr_txpd_final_data  CLOB;

    --HSN cursor local variables
   l_char_hsn_start      VARCHAR2(220) := ',"hsn": {'||CHR(10)||'"data" : [';
   l_char_hsn_end        VARCHAR2(220) := ']'||CHR(10)||'}';
   l_chr_hsn_data        VARCHAR2(3000);
   l_chr_hsn_map_data    CLOB;
   l_num_rownum9         NUMBER;
   l_chr_hsn_final_data  CLOB;

 --CDNR cursor local variables
   l_char_cdnr_start     VARCHAR2(220) := ',"cdnr": [';
   l_char_cdnr_end       VARCHAR2(220) := ']';
   l_chr_cdnr_data       VARCHAR2(32000);
   l_chr_cdnr1_data      VARCHAR2(32000);
   l_chr_cdnr1_e_data    VARCHAR2(32000);
   l_chr_cdnr_map_data   CLOB;
   l_chr_cdnr1_map_data  CLOB;
   l_num_rownum10         NUMBER;
   l_num_rownum11         NUMBER;
   l_chr_cdnr_final_data  CLOB;

 --CDNUR cursor local variables
   l_char_cdnur_start      VARCHAR2(220) := ',"cdnur": [';
   l_char_cdnur_end        VARCHAR2(220) := ']';
   l_chr_cdnur_data        VARCHAR2(32000);
   l_chr_cdnur_map_data    clob;
   l_num_rownum12          NUMBER;
   l_chr_cdnur_final_data  clob;

    --DOC cursor local variables
   l_char_doc_start      VARCHAR2(220) := ',"doc_issue": {'||CHR(10)||'"doc_det": [';
   l_char_doc_end        VARCHAR2(220) := ']'||CHR(10)||'}';
   l_chr_doc_data        VARCHAR2(32000);
   l_chr_doc_map_data    clob;
   l_num_rownum13        NUMBER;
   l_chr_doc_final_data  clob;

    --B2CL cursor local variables
   l_char_b2cl_start      VARCHAR2(220) := ',"b2cl": ['||CHR(10)||'{';
   l_char_b2cl_end        VARCHAR2(220) := '}'||CHR(10)||']';
   l_chr_b2cl_data        VARCHAR2(32000);
   l_chr_b2cl1_data       VARCHAR2(32000);
   l_chr_b2cl1_e_data     VARCHAR2(32000);
   l_chr_b2cl_map_data    CLOB;
   l_num_rownum14        NUMBER;
   l_num_rownumb2cl      NUMBER;
   l_chr_b2cl_final_data  CLOB;

   --B2CS local variables
   l_char_b2cs_start    VARCHAR2(20) := ',"b2cs": [';
   l_char_b2cs_end     VARCHAR2(20) := ']';
   l_chr_b2cs_data      VARCHAR2(3000);
   l_chr_b2cs_map_data  CLOB;
   l_num_rownum15       NUMBER;
   l_chr_b2cs_final_data CLOB;
   l_null_var           VARCHAR2(20);
   v_conc_req_id      NUMBER;

   lv_wallet_password  VARCHAR2(200) := NULL;
   lv_wallet_path      VARCHAR2(200) := NULL;

   v_tk_url            VARCHAR2(1000);
   v_ocp_key            VARCHAR2(1000);
   v_client_id          VARCHAR2(1000);
   v_client_secret      VARCHAR2(1000);

   v_save_url    VARCHAR2(1000);
    v_gstinno     VARCHAR2(1000);
    v_reference_no     VARCHAR2(1000);
	v_resp          XMLTYPE;
	v_connection       varchar2(20);
v_error_message    varchar2(500);

   BEGIN
     gv_access_token  := NULL;
     l_chr_final_data := NULL;
     g_period_no      := p_period;

     fnd_file.put_line(fnd_file.log,'Starting main proc A ');
     fnd_file.put_line(fnd_file.log,'Period Date: '||p_period);

      l_num_rownum1:=0;

     FOR r_main_cur IN main_cur LOOP
     l_chr_main_start := NULL;
     l_chr_b2b_start := NULL;


       fnd_file.put_line(fnd_file.log,'Starting Loop main_cur ');


       l_chr_main_end   := NULL;

       l_chr_main_start :=
        '{'||'"gstValue":'||'{' ||CHR(10)||'"gstin":'||'"'||r_main_cur.gstn||'",'
                                ||CHR(10)||'"fp":'||'"'||r_main_cur.fp1||'",'
                                ||CHR(10)||'"gt":'||''||r_main_cur.gt||','
                                ||CHR(10)||'"cur_gt":'||''||r_main_cur.cur_gt;

            l_chr_b2b_start:=   CHR(10)||',"b2b": [';

               l_chr_ctin_final_data := empty_clob();
               l_b2b_merge_data := empty_clob();
               l_num_ctin            := 0;


              FOR r_ctin_b2b_extract IN ctin_b2b_extract(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt)
              LOOP

                    l_chr_ctin_data:=NULL;
                    l_num_ctin := l_num_ctin +1;

                  l_chr_ctin_data   := CHR(10)||'{'
                                              ||CHR(10)|| '"ctin":'||'"'||r_ctin_b2b_extract.ctin||'",'
                                              ||CHR(10)||'"inv": [';


                    l_chr_invb2b_data       := empty_clob();
                    l_num_invb2b            := 0;
                    l_chr_invb2b_final_data := empty_clob();
                    l_num_inv_tot_rec       := 0;

               FOR r_b2b_inv IN b2b_inv(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt,r_ctin_b2b_extract.ctin)
               LOOP

                   l_num_invb2b :=l_num_invb2b+1;
                   l_chr_invb2b_data   := CHR(10)||'{'
                     ||CHR(10)||'"inum":"'||r_b2b_inv.inum1||'",'
                     ||CHR(10)||'"idt":"' ||r_b2b_inv.idt||'",'
                     ||CHR(10)||'"val":'  ||r_b2b_inv.val||','
                     ||CHR(10)||'"rchrg":"'||r_b2b_inv.rchrg||'",'
                     ||CHR(10)||'"pos":"'||r_b2b_inv.pos||'",'
                     ||CHR(10)||'"inv_typ":"'||r_b2b_inv.inv_typ||'",'
                     ||CHR(10)||'"itms": [';

                     l_chr_itemb2b_data       := empty_clob();
                     l_num_itemb2b            := 0;
                     l_chr_itemb2b_final_data := empty_clob();

               FOR r_b2b_item IN b2b_item(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt,r_b2b_inv.ctin,r_b2b_inv.inum)
               LOOP
                      l_num_itemb2b      := l_num_itemb2b+1;

            l_chr_itemb2b_data := CHR(10)||'{'
                                  ||CHR(10)||'"num":"'||l_num_itemb2b||'",'
                                  ||CHR(10)||'"itm_det":{'
                                  ||CHR(10)||'"rt":'||r_b2b_item.rt||','
                                  ||CHR(10)||'"txval":'||r_b2b_item.txval||','
                                  ||CHR(10)||'"iamt":'||r_b2b_item.iamt||','
                                  ||CHR(10)||'"camt":'||r_b2b_item.camt||','
                                  ||CHR(10)||'"samt":'||r_b2b_item.samt||','
                                  ||CHR(10)||'"csamt":'||r_b2b_item.csamt||CHR(10)||'}';

           IF l_num_itemb2b =1 THEN
              l_chr_itemb2b_final_data := l_chr_itemb2b_data||CHR(10)||l_chr_itemb2b_end;
            ELSE
              l_chr_itemb2b_final_data := l_chr_itemb2b_final_data||','||l_chr_itemb2b_data||CHR(10)||l_chr_itemb2b_end;
            END IF;
           END LOOP;

          IF l_num_invb2b =1 THEN
            l_chr_invb2b_final_data := l_chr_invb2b_data||l_chr_itemb2b_final_data||CHR(10)||l_chr_invb2b_end;
          ELSE
            l_chr_invb2b_final_data := l_chr_invb2b_final_data||','||CHR(10)||l_chr_invb2b_data||l_chr_itemb2b_final_data||CHR(10)||l_chr_invb2b_end;
          END IF;
        END LOOP;

        IF l_num_ctin =1 THEN
           l_chr_ctin_final_data := l_chr_ctin_data||l_chr_invb2b_final_data||CHR(10)||l_chr_ctin_end;
        ELSE
           l_chr_ctin_final_data := l_chr_ctin_final_data||','||CHR(10)||l_chr_ctin_data||l_chr_invb2b_final_data||CHR(10)||l_chr_ctin_end;
        END IF;
      END LOOP;


              IF l_chr_invb2b_final_data IS NULL
      THEN
      l_b2b_merge_data := l_null_var ;
      ELSE
                l_b2b_merge_data :=l_chr_b2b_start||l_chr_ctin_final_data||CHR(10)||l_chr_b2b_end;

                END IF;

            l_chr_nil_map_data := NULL;
            l_num_rownum5      := 0;

            FOR r_nil IN nil(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP


                   l_num_rownum5  := l_num_rownum5+1;
                   l_chr_nil_data := NULL;
                   l_chr_nil_data := '{' ||CHR(10)||'"sply_ty" :'||'"'||r_nil.sply_ty||'",'
                                         ||CHR(10)||'"expt_amt" :'||r_nil.expt_amt||','
                                         ||CHR(10)||'"nil_amt" :'||r_nil.nil_amt||','
                                         ||CHR(10)||'"ngsup_amt" :'||r_nil.ngsup_amt||CHR(10)||'}';

                   IF l_num_rownum5 = 1 THEN
                      l_chr_nil_map_data := l_chr_nil_data;
                   ELSE
                      l_chr_nil_map_data := l_chr_nil_map_data||','||CHR(10)||l_chr_nil_data;
                   END IF;
            END LOOP;
IF l_chr_nil_map_data IS NULL
      THEN
      l_chr_nil_final_data := l_null_var ;
      ELSE
                   l_chr_nil_final_data:= CHR(10)||l_char_nil_start ||CHR(10)||l_char_inv1_start|| l_chr_nil_map_data||CHR(10)||l_char_inv1_end||CHR(10)||l_char_nil_end;

END IF;
           l_chr_exp_map_data := NULL;
           l_char_exptwo_start:=NULL;
            l_num_rownum6      := 0;
            FOR r_export IN Export(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP



               l_char_exptwo_start  := '{'||CHR(10)||'"exp_typ": '||'"'||r_export.export_type||'",'||CHR(10)||'"inv": [';

                l_num_rownum6     :=  l_num_rownum6+1;
                l_chr_expwo_data  :=  NULL;

                   l_chr_expwo_data := '{' ||CHR(10)||'"inum" :'||'"'||r_export.inum||'",'
                                         ||CHR(10)||'"idt" :'||'"'||r_export.idt||'",'
                                         ||CHR(10)||'"val" :'||'"'||r_export.val||'",'
                                         ||CHR(10)||'"sbnum" :'||'"'||r_export.sbnum||'",'
                                         ||CHR(10)||'"sbdt" :'||'"'||r_export.sbnum||'",'
                                         ||CHR(10)||'"itms": ['||CHR(10)||'{'
                                         ||CHR(10)||'"txval" :'||''||r_export.txval||','
                                         ||CHR(10)||'"rt" :'||'"'||r_export.rt||'",'
                                         ||CHR(10)||'"iamt" :'||''||r_export.iamt||''||CHR(10)||'}'||CHR(10)||']'||CHR(10)||'}'; ---added by shalini


                   IF l_num_rownum6 = 1 THEN
                      l_chr_exp_map_data := l_char_exptwo_start||CHR(10)||l_chr_expwo_data||l_char_exptwo_end;
                   ELSE
                      l_chr_exp_map_data := l_chr_exp_map_data||','||CHR(10)||l_char_exptwo_start||CHR(10)||l_chr_expwo_data||l_char_exptwo_end;
                   END IF;
            END LOOP;

IF l_chr_exp_map_data IS NULL
      THEN
      l_chr_exp_final_data := l_null_var ;
      ELSE
                   l_chr_exp_final_data:= CHR(10)||l_char_exp_start||l_chr_exp_map_data||CHR(10)||l_char_exp_end;
                   END IF;

            l_chr_at_map_data := NULL;
            l_num_rownum7      := 0;

            FOR r_at IN Adv_tax(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP


                   l_num_rownum7  := l_num_rownum7+1;
                   l_chr_at_data := NULL;
                   l_chr_at_data := '{' ||CHR(10)||'"pos" :'||'"'||r_at.pos||'",'
                                         ||CHR(10)||'"sply_ty" :'||'"'||r_at.sply_ty||'",'
                                         ||CHR(10)||'"itms" : ['
                                              ||CHR(10)||'{'
                                              ||CHR(10)||'"rt" :'||r_at.rt||','
                                              ||CHR(10)||'"ad_amt" :'||''||r_at.ad_amt||','
                                              ||CHR(10)||'"iamt" :'||''||r_at.iamt||','
                                              ||CHR(10)||'"camt" :'||''||r_at.camt||','
                                              ||CHR(10)||'"samt" :'||''||r_at.samt||','
                                              ||CHR(10)||'"csamt" :'||''||r_at.csamt||''
                                              ||CHR(10)||'}'
                                              ||CHR(10)||']'
                                   ||CHR(10)||'}';

                   IF l_num_rownum7 = 1 THEN
                      l_chr_at_map_data := l_chr_at_data;
                   ELSE
                      l_chr_at_map_data := l_chr_at_map_data||','||CHR(10)||l_chr_at_data;
                   END IF;
            END LOOP;

IF l_chr_at_map_data IS NULL
      THEN
      l_chr_at_final_data := l_null_var ;
      ELSE
                   l_chr_at_final_data:= CHR(10)||l_char_at_start ||CHR(10)||l_chr_at_map_data||CHR(10)||l_char_at_end;

END IF;
            l_chr_txpd_map_data := NULL;
            l_num_rownum8       := 0;

            FOR r_txpd IN txpd(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP


                   l_num_rownum8  := l_num_rownum8+1;
                   l_chr_txpd_data := NULL;
                   l_chr_txpd_data := '{' ||CHR(10)||'"pos" :'||'"'||r_txpd.pos||'",'
                                         ||CHR(10)||'"sply_ty" :'||'"'||r_txpd.sply_ty||'",'
                                         ||CHR(10)||'"itms" : ['
                                              ||CHR(10)||'{'
                                              ||CHR(10)||'"rt" :'||'"'||r_txpd.rt||'",'
                                              ||CHR(10)||'"ad_amt" :'||''||r_txpd.ad_amt||','
                                              ||CHR(10)||'"iamt" :'||''||r_txpd.iamt||','
                                              ||CHR(10)||'"camt" :'||''||r_txpd.camt||','
                                              ||CHR(10)||'"samt" :'||''||r_txpd.samt||','
                                              ||CHR(10)||'"csamt" :'||''||r_txpd.csamt||''
                                              ||CHR(10)||'}'
                                              ||CHR(10)||']'
                                   ||CHR(10)||'}';

                   IF l_num_rownum8 = 1 THEN
                      l_chr_txpd_map_data := l_chr_txpd_data;
                   ELSE
                      l_chr_txpd_map_data := l_chr_txpd_map_data||','||CHR(10)||l_chr_txpd_data;
                   END IF;
            END LOOP;
IF l_chr_txpd_map_data IS NULL
      THEN
      l_chr_txpd_final_data := l_null_var ;
      ELSE
                   l_chr_txpd_final_data:= CHR(10)||l_char_txpd_start ||CHR(10)||l_chr_txpd_map_data||CHR(10)||l_char_txpd_end;
END IF;

            l_chr_hsn_map_data := NULL;
            l_num_rownum9       := 0;

            FOR r_hsn IN hsn(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP


                   l_num_rownum9  := l_num_rownum9+1;
                   l_chr_hsn_data := NULL;
                   l_chr_hsn_data := '{' ||CHR(10)||'"num" :'||'"'||l_num_rownum9||'",'
                                         ||CHR(10)||'"hsn_sc" :'||'"'||r_hsn.hsn_sc||'",'
                                         ||CHR(10)||'"desc" :'||'"'||r_hsn.desc1||'",'
                                         ||CHR(10)||'"uqc" :'||'"'||r_hsn.uqc||'",'
                                         ||CHR(10)||'"qty" :'||'"'||r_hsn.qty||'",'
                                         ||CHR(10)||'"val" :'||'"'||r_hsn.val||'",'
                                         ||CHR(10)||'"txval" :'||''||r_hsn.txval||','
                                         ||CHR(10)||'"iamt" :'||''||r_hsn.iamt||','
                                         ||CHR(10)||'"camt" :'||''||r_hsn.camt||','
                                         ||CHR(10)||'"samt" :'||''||r_hsn.samt||','
                                         ||CHR(10)||'"csamt" :'||''||r_hsn.csamt||''
                                         ||CHR(10)||'}';

                   IF l_num_rownum9 = 1 THEN
                      l_chr_hsn_map_data := l_chr_hsn_data;
                   ELSE
                      l_chr_hsn_map_data := l_chr_hsn_map_data||','||CHR(10)||l_chr_hsn_data;
                   END IF;
            END LOOP;
IF l_chr_hsn_map_data IS NULL
      THEN
      l_chr_hsn_final_data := l_null_var ;
      ELSE

                   l_chr_hsn_final_data:= CHR(10)||l_char_hsn_start ||CHR(10)||l_chr_hsn_map_data||CHR(10)||l_char_hsn_end;
END IF;

            l_chr_cdnr1_map_data := NULL;
            l_num_rownum10       := 0;

            FOR r_cdnr_main_cur IN cdnr_main_cur(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP



                   l_num_rownum10       := l_num_rownum10+1;
                   l_chr_cdnr1_data     := NULL;
                   l_chr_cdnr1_e_data   := NULL;
                   l_chr_cdnr_map_data  := NULL;
                   l_num_rownum11       := 0;

                   l_chr_cdnr1_data   := '{' ||CHR(10)||'"ctin":'||'"'||r_cdnr_main_cur.ctin||'",'||CHR(10)||' "nt": ['||CHR(10);
                   l_chr_cdnr1_e_data := CHR(10)||']'||CHR(10)||'}';

                     FOR r_cdnr_child_cur IN cdnr_child_cur(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt,r_cdnr_main_cur.ctin) LOOP


                         l_chr_cdnr_data := NULL;

                               l_num_rownum11  := l_num_rownum11+1;
                               l_chr_cdnr_data := NULL;
                               l_chr_cdnr_data := '{' ||CHR(10)||'"flag" :'||'"'||r_cdnr_child_cur.flag||'",'
                                                     ||CHR(10)||'"ntty" :'||'"'||r_cdnr_child_cur.ntty||'",'
                                                     ||CHR(10)||'"nt_num" :'||'"'||r_cdnr_child_cur.nt_num||'",'
                                                     ||CHR(10)||'"nt_dt" :'||'"'||r_cdnr_child_cur.nt_dt||'",'
                                                          ||CHR(10)||'"rsn" :'||'"'||r_cdnr_child_cur.rsn||'",'
                                                               ||CHR(10)||'"p_gst" :'||'"'||r_cdnr_child_cur.p_gst||'",'
                                                     ||CHR(10)||'"inum" :'||'"'||r_cdnr_child_cur.inum||'",'
                                                     ||CHR(10)||'"idt" :'||'"'||r_cdnr_child_cur.idt||'",'
                                                     ||CHR(10)||'"val" :'||'"'||r_cdnr_child_cur.val||'",'
                                                     ||CHR(10)||'"itms": [ '||CHR(10)||'{'
                                                              ||CHR(10)||'"num" :'||'"'||l_num_rownum11||'",'
                                                               ||CHR(10)||'"itm_det": { '
                                                              ||CHR(10)||'"rt" :'||'"'||r_cdnr_child_cur.rt||'",'
                                                              ||CHR(10)||'"txval" :'||''||r_cdnr_child_cur.txval||','
                                                              ||CHR(10)||'"iamt" :'||''||r_cdnr_child_cur.iamt||','
                                                              ||CHR(10)||'"camt" :'||''||r_cdnr_child_cur.camt||','
                                                              ||CHR(10)||'"samt" :'||''||r_cdnr_child_cur.samt||','
                                                              ||CHR(10)||'"csamt" :'||''||r_cdnr_child_cur.csamt||''
                                                              ||CHR(10)||'}'||CHR(10)||'}'||CHR(10)||']'
                                                     ||CHR(10)||'}';

                               IF l_num_rownum11 = 1 THEN
                                  l_chr_cdnr_map_data := l_chr_cdnr_data;
                               ELSE
                                  l_chr_cdnr_map_data := l_chr_cdnr_map_data||','||CHR(10)||l_chr_cdnr_data;
                               END IF;
                   END LOOP;

                IF l_num_rownum10 = 1 THEN
                    l_chr_cdnr1_map_data := l_chr_cdnr1_data||l_chr_cdnr_map_data||l_chr_cdnr1_e_data;
                ELSE
                    l_chr_cdnr1_map_data := l_chr_cdnr1_map_data||','||CHR(10)||l_chr_cdnr1_data||l_chr_cdnr_map_data||l_chr_cdnr1_e_data;
                END IF;

            END LOOP;
IF l_chr_cdnr1_map_data IS NULL
      THEN
      l_chr_cdnr_final_data := l_null_var ;
      ELSE
                   l_chr_cdnr_final_data:= CHR(10)||l_char_cdnr_start ||CHR(10)||l_chr_cdnr1_map_data||CHR(10)||l_char_cdnr_end;
END IF;
            l_chr_cdnur_map_data := NULL;
            l_num_rownum12       := 0;

            FOR r_cdnur IN cdnur(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP


                   l_num_rownum12  := l_num_rownum12+1;
                   l_chr_cdnur_data := NULL;

                   l_chr_cdnur_data := '{' ||CHR(10)||'"flag" :'||'"'||r_cdnur.flag||'",'
                                           ||CHR(10)||'"typ" :'||'"'||r_cdnur.typ||'",'
                                           ||CHR(10)||'"ntty" :'||'"'||r_cdnur.ntty||'",'
                                                    ||CHR(10)||'"nt_num" :'||'"'||r_cdnur.nt_num||'",'
                                                    ||CHR(10)||'"nt_dt" :'||'"'||r_cdnur.nt_dt||'",'
                                                     ||CHR(10)||'"rsn" :'||'"'||r_cdnur.rsn||'",'
                                                      ||CHR(10)||'"p_gst" :'||'"'||r_cdnur.p_gst||'",'
                                                    ||CHR(10)||'"inum" :'||'"'||r_cdnur.inum||'",'
                                                    ||CHR(10)||'"idt" :'||'"'||r_cdnur.idt||'",'
                                                    ||CHR(10)||'"val" :'||'"'||r_cdnur.val||'",'
                                                    ||CHR(10)||'"itms": [ '||CHR(10)||'{'
                                                    ||CHR(10)||'"num" :'||'"'||l_num_rownum12||'",'
                                                               ||CHR(10)||'"itm_det": { '
                                                             ||CHR(10)||'"rt" :'||'"'||r_cdnur.rt||'",'
                                                             ||CHR(10)||'"txval" :'||''||r_cdnur.txval||','
                                                             ||CHR(10)||'"iamt" :'||''||r_cdnur.iamt||','
                                                             ||CHR(10)||'"camt" :'||''||r_cdnur.camt||','
                                                             ||CHR(10)||'"samt" :'||''||r_cdnur.samt||','
                                                             ||CHR(10)||'"csamt" :'||''||r_cdnur.csamt||''
                                                             ||CHR(10)||'}'||CHR(10)||'}'||CHR(10)||']'
                                                     ||CHR(10)||'}';

                   IF l_num_rownum12 = 1 THEN
                      l_chr_cdnur_map_data := l_chr_cdnur_data;
                   ELSE
                      l_chr_cdnur_map_data := l_chr_cdnur_map_data||','||CHR(10)||l_chr_cdnur_data;
                   END IF;
            END LOOP;
IF l_chr_cdnur_map_data IS NULL
      THEN
      l_chr_cdnur_final_data := l_null_var ;
      ELSE
                   l_chr_cdnur_final_data:= CHR(10)||l_char_cdnur_start ||CHR(10)||l_chr_cdnur_map_data||CHR(10)||l_char_cdnur_end;

                   END IF;

            l_chr_doc_map_data := NULL;
            l_num_rownum13       := 0;

            FOR r_doc IN doc(r_main_cur.gstn) LOOP

              fnd_file.put_line(fnd_file.log,'Starting Loop doc ');
                   l_num_rownum13  := l_num_rownum13+1;
                   l_chr_doc_data := NULL;

                   l_chr_doc_data := '{' ||CHR(10)||'"doc_num" :'||1||','
                                                  ||CHR(10)||'"docs": [ '||CHR(10)||'{'
                                                           ||CHR(10)||'"num" :'||'"'||l_num_rownum13||'",'
                                                           ||CHR(10)||'"from" :'||'"'||r_doc.from1||'",'
                                                           ||CHR(10)||'"to" :'||'"'||r_doc.to1||'",'
                                                           ||CHR(10)||'"totnum" :'||'"'||r_doc.totnum||'",'
                                                           ||CHR(10)||'"cancel" :'||'"'||r_doc.cancel||'",'
                                                           ||CHR(10)||'"net_issue" :'||'"'||r_doc.net_issue||'"'
                                                           ||CHR(10)||'}'||CHR(10)||']'
                                                   ||CHR(10)||'}';

                   IF l_num_rownum13 = 1 THEN
                      l_chr_doc_map_data := l_chr_doc_data;
                   ELSE
                      l_chr_doc_map_data := l_chr_doc_map_data||','||CHR(10)||l_chr_doc_data;
                   END IF;
            END LOOP;

            IF l_chr_doc_map_data IS NULL
      THEN
      l_chr_doc_final_data := l_null_var ;
      ELSE


                   l_chr_doc_final_data:= CHR(10)||l_char_doc_start ||CHR(10)||l_chr_doc_map_data||CHR(10)||l_char_doc_end;
END IF;

            l_chr_b2cl_map_data := NULL;
            l_num_rownum14      := 0;

            FOR r_b2cl IN b2cl(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP

              fnd_file.put_line(fnd_file.log,'Starting Loop b2cl ');
                   l_num_rownum14  := l_num_rownum14+1;
                   l_chr_b2cl_data := NULL;
                   l_chr_b2cl1_data := NULL;

                   l_chr_b2cl1_data := '"pos": '||'"'||SUBSTR(r_main_cur.gstn,1,2)||'",'||CHR(10)||'"inv": [';
                   l_chr_b2cl1_e_data := ']';

                   l_chr_b2cl_data := '{' ||CHR(10)||'"flag" :'||'"'||r_b2cl.flag||'",'
                                          ||CHR(10)||'"inum" :'||'"'||r_b2cl.inum||'",'
                                          ||CHR(10)||'"idt" :'||'"'||r_b2cl.idt||'",'
                                          ||CHR(10)||'"val" :'||'"'||r_b2cl.val||'",'
                                          ||CHR(10)||'"etin" :'||'"'||r_b2cl.etin||'",'
                                                   ||CHR(10)||'"itms": [ '||CHR(10)||'{'
                                                           ||CHR(10)||'"num" :'||'"'||r_b2cl.num||'",'
                                                           ||CHR(10)||'"itm_det" :'||'{'
                                                                    ||CHR(10)||'"rt" :'||''||r_b2cl.rt||','
                                                                    ||CHR(10)||'"txval" :'||''||r_b2cl.txval||','
                                                                    ||CHR(10)||'"iamt" :'||''||r_b2cl.iamt||','
                                                                    ||CHR(10)||'"csamt" :'||''||r_b2cl.csamt||''
                                                                    ||CHR(10)||'}'
                                                           ||CHR(10)||'}'||CHR(10)||']'
                                                   ||CHR(10)||'}';

                   IF l_num_rownum14 = 1 THEN
                      l_chr_b2cl_map_data := l_chr_b2cl_data;
                   ELSE
                      l_chr_b2cl_map_data := l_chr_b2cl_map_data||','||CHR(10)||l_chr_b2cl_data;
                   END IF;
            END LOOP;
IF l_chr_b2cl_map_data IS NULL
      THEN
      l_chr_b2cl_final_data := l_null_var ;
      ELSE
                   l_chr_b2cl_final_data:= CHR(10)||l_char_b2cl_start||CHR(10)||l_chr_b2cl1_data||CHR(10)||l_chr_b2cl_map_data||CHR(10)||l_chr_b2cl1_e_data||CHR(10)||l_char_b2cl_end;
END IF;

                    l_chr_b2cs_map_data := NULL;
            l_num_rownum15      := 0;

            FOR r_b2cs IN b2cs(r_main_cur.gstn,r_main_cur.gt,r_main_cur.cur_gt) LOOP

              fnd_file.put_line(fnd_file.log,'Starting Loop b2cs ');
                   l_num_rownum15  := l_num_rownum15+1;
                   l_chr_b2cs_data := NULL;
                   l_chr_b2cs_data := '{' ||CHR(10)||'"sply_ty" :'||'"'||r_b2cs.sply_ty||'",'
                                         ||CHR(10)||'"rt" :"'||r_b2cs.rt||'",'
                                         ||CHR(10)||'"typ" :"'||r_b2cs.typ||'",'
                                         ||CHR(10)||'"etin" :"'||r_b2cs.etin||'",'
                                         ||CHR(10)||'"pos" :"'||r_b2cs.pos||'",'
                                         ||CHR(10)||'"txval" :'||r_b2cs.txval||','
                                         ||CHR(10)||'"iamt" :'||r_b2cs.iamt||','
                                         ||CHR(10)||'"camt" :'||r_b2cs.camt||','
                                         ||CHR(10)||'"samt" :'||r_b2cs.samt||','
                                         ||CHR(10)||'"csamt" :'||r_b2cs.csamt||CHR(10)||'}';

                   IF l_num_rownum15 = 1 THEN
                      l_chr_b2cs_map_data := l_chr_b2cs_data;
                   ELSE
                      l_chr_b2cs_map_data := l_chr_b2cs_map_data||','||CHR(10)||l_chr_b2cs_data;
                   END IF;
            END LOOP;

            IF l_chr_b2cs_map_data IS NULL
      THEN
      l_chr_b2cs_final_data := l_null_var ;
      ELSE
                   l_chr_b2cs_final_data:= CHR(10)||l_char_b2cs_start ||CHR(10)|| l_chr_b2cs_map_data||CHR(10)||l_char_b2cs_end;

END IF;

         l_chr_main_end := CHR(10)||'}'||'}';
        l_num_rownum1 := l_num_rownum1+1;


              l_chr_final_data := l_chr_main_start||l_b2b_merge_data||l_chr_nil_final_data||l_chr_exp_final_data||l_chr_at_final_data||l_chr_txpd_final_data||l_chr_hsn_final_data||l_chr_cdnr_final_data||l_chr_cdnur_final_data||l_chr_doc_final_data||l_chr_b2cl_final_data||l_chr_b2cs_final_data||l_chr_main_end; --']' Removed by venkatesh 10-Mar-2018



        v_conc_req_id:=fnd_global.conc_request_id;

   fnd_file.put_line(fnd_file.log,'REQUEST_ID: '||v_conc_req_id);

  UPDATE XXATUIND_GSTR1_MAIN_TAB
      SET REQUEST_ID  =  v_conc_req_id
     WHERE 1=1
     AND batch_id=P_BATCH_ID
     AND PERIOD_NAME=p_period
     AND NVL(PROCESS_STATUS,'I')='I'
     AND FIRST_PARTY_PRIMARY_REG_NUM=r_main_cur.gstn;


           COMMIT;


      END LOOP;

      fnd_file.put_line(fnd_file.log,l_chr_final_data);

--Wallet path details to call http request

     BEGIN
	    SELECT description
	      INTO lv_wallet_path
          FROM fnd_lookup_values_vl
         WHERE lookup_type = 'XXATUIND_GSTRWALLET_LKP'
           AND   enabled_flag = 'Y'
           AND   lookup_code = 'PATH';
      EXCEPTION
        WHEN OTHERS THEN
		  lv_wallet_path := NULL;
		  fnd_file.put_line(fnd_file.log,'Exception while getting the wallet Path '||SQLERRM);
      END;

   	 BEGIN

       SELECT description
	     INTO lv_wallet_password
         FROM fnd_lookup_values_vl
        WHERE lookup_type = 'XXATUIND_GSTRWALLET_LKP'
          AND enabled_flag = 'Y'
          AND lookup_code = 'PASSWORD';
	  EXCEPTION
        WHEN OTHERS THEN
		  lv_wallet_password := NULL;
		  fnd_file.put_line(fnd_file.log,'Exception while getting the wallet Path '||SQLERRM);
      END;


      --BEGIN
      --  UTL_HTTP.set_wallet('file:/oracle/R12PPS1/db/tech_st/11.2.0/owm/wallets/wallets_GSTR','we1c0me456');
      --EXCEPTION
      --  WHEN OTHERS THEN
      --    fnd_file.put_line(fnd_file.log,'Exception while settng the wallet '||SQLERRM);
      --END;

	  v_error_message := null;



--To get token url
	    BEGIN

	    SELECT description
	    INTO   v_tk_url
        FROM   fnd_lookup_values_vl
       WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'TK_URL';
      EXCEPTION
      WHEN OTHERS THEN
      v_tk_url := NULL;
	 --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the TK_URL '||SQLERRM);
      END;

	 	--To get Subscription-Key

   	 BEGIN

       SELECT description
	      INTO v_ocp_key
         FROM  fnd_lookup_values_vl
      WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'OCP-APIM-SUBSCRIPTION-KEY';

	  EXCEPTION
        WHEN OTHERS THEN
		  v_ocp_key := NULL;
		  --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the Ocp-Apim-Subscription-Key '||SQLERRM);
      END;

	  --To get client_id

	   BEGIN

       SELECT description
	      INTO v_client_id
         FROM  fnd_lookup_values_vl
      WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'CLIENT_ID';

	  EXCEPTION
        WHEN OTHERS THEN
		  v_client_id := NULL;
		  --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the client_id '||SQLERRM);
      END;


	  --To get client_secret

	   BEGIN

       SELECT description
	      INTO v_client_secret
         FROM  fnd_lookup_values_vl
      WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'CLIENT_SECRET';

	  EXCEPTION
        WHEN OTHERS THEN
		  v_client_secret := NULL;
		  --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the client_secret '||SQLERRM);
      END;

	  --To get SAVE_URL


	    BEGIN
	    SELECT description
	    INTO   v_save_url
        FROM   apps.fnd_lookup_values_vl
       WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'SAVE_URL';
      EXCEPTION
        WHEN OTHERS THEN
		  v_save_url := NULL;
		  --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the SAVE_URL '||SQLERRM);
      END;


	  --To get GSTINNO

	   BEGIN

       SELECT description
	      INTO v_gstinno
         FROM  apps.fnd_lookup_values_vl
      WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'GSTINNO';

	  EXCEPTION
        WHEN OTHERS THEN
		  v_gstinno := NULL;
		  --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the GSTINNO '||SQLERRM);
      END;


	  --To get REFERENCE_NO

	   BEGIN

       SELECT description
	      INTO v_reference_no
         FROM  apps.fnd_lookup_values_vl
      WHERE   lookup_type = 'XXATUIND_GSTR1_TK_PARAM'
          AND  enabled_flag = 'Y'
	      AND  lookup_code = 'REFERENCE_NO';

	  EXCEPTION
        WHEN OTHERS THEN
		  v_reference_no := NULL;
		  --apps.fnd_file.put_line(apps.fnd_file.log,'Exception while getting the REFERENCE_NO '||SQLERRM);
      END;

     v_connection := null;
	 v_error_message := null;
     fnd_file.put_line(fnd_file.log,'Calling XXATUIND_GSTR1_JSON_UTIL_PKG get_token_proc and gstr1_save_proc');
      v_resp := XXWEB.XXATUIND_GSTR1_JSON_UTIL_PKG.gstr1_save_proc(p_wallet_path    => lv_wallet_path
	                                                              ,p_wallet_pwd    => lv_wallet_password
	                                                              ,p_json_data     => l_chr_final_data
	                                                              ,p_period        => p_period
	                                                              ,p_batch_id      => p_batch_id 
	                                                              ,p_token         => NULL    
	                                                              ,p_save_url      => v_save_url 
	                                                              ,p_ocp_key       => v_ocp_key
	                                                              ,p_gstinno       => v_gstinno  
	                                                              ,p_reference_no  => v_reference_no
	                                                              ,p_connection    => v_connection
	                                                              ,p_error_message => v_error_message);
																  
 --Call The gstr1_save_proc to process JSON data to wepsol portal


	fnd_file.put_line(fnd_file.log,'v_error_message :::  '||v_error_message);

      --Calling  XML_RESPONSE_UPDATE_PROC procedure to update response in stage table
	 XML_RESPONSE_UPDATE_PROC(P_BATCH_ID => P_BATCH_ID,
							  P_PERIOD => p_period,
							  P_FIRST_PARTY_REG_NUM =>p_first_party_prim_reg_num,
							  P_XML_RESP =>v_resp
							  ) ;

   EXCEPTION
     WHEN OTHERS THEN
      fnd_file.put_line(fnd_file.log,'Exception occured while calling main Proc '||SQLERRM);
   END process_jason_format_proc;

   PROCEDURE insert_ebs_data_proc(
                                   p_period        IN VARCHAR2
                                 )

   AS
    /********************************************************************************************************************
    * Type                : Procedure
    * Name                : insert_ebs_data_proc
    * Input Parameters    :  1.p_period
    *
    * Purpose             : Generate GST1 Data like Json format
    * Company             : Transform Edge
    * Created By          : Rohit Prasad
    * Created Date        : 16-Feb-2018
    * Last Reviewed By    :
    * Last Reviewed Date  :
    ********************************************************************************************************************
    * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
    * Date        By               Script   MKS        By                Date         Type     Details
    * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
    * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
    ********************************************************************************************************************/
   --
   CURSOR main_cur
   IS
    SELECT
           SUBSTR(first_party_primary_reg_num,1,15)  gstn,
           TO_CHAR(trx_date,'MMYYYY') fp1,
           ROUND(SUM (rounded_taxable_amt_fun_curr),2) gt,
           ROUND(SUM ((NVL(CURRENCY_CONVERSION_RATE,1) * line_amt)+ ROUNDED_TAX_AMT_FUN_CURR),2) cur_gt   --added by shalini on 16-mar
      FROM XXATUIND_GSTR1_TAB
     WHERE entity_code LIKE 'TRANS%'
      AND  TO_CHAR(trx_date,'MMYYYY')  = g_period_no
 GROUP BY  SUBSTR(first_party_primary_reg_num,1,15),
           TO_CHAR(trx_date,'MMYYYY');




   CURSOR ctin_b2b_extract(p_first_party_prim_reg_num IN VARCHAR2
                           )
   IS
    SELECT SUBSTR(first_party_primary_reg_num,1,15)         gstn,
           SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,15)         ctin
      FROM XXATUIND_GSTR1_TAB
     WHERE 1=1
      AND  entity_code LIKE 'TRANS%'
      AND  TO_CHAR(trx_date,'MMYYYY')               = g_period_no
      AND  SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
  GROUP BY first_party_primary_reg_num,
           third_party_primary_reg_num;


   CURSOR b2b_inv(p_first_party_prim_reg_num IN VARCHAR2,
                  p_third_pty_prim_reg_num   IN VARCHAR2
                  )
   IS
    SELECT SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,15) ctin,
           jtl.PARTY_NAME                  itm_det,
           NVL(TAX_INVOICE_NUM,trx_number) inum,
           trx_date                        idt,
           XXATUIND_GSTR1_JSON_PKG.get_tax_invoice_value(jtl.trx_id,null) val,
           SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,2) pos,
           'N' rchrg,
           'R' inv_typ,
           first_party_primary_reg_num gstn
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
       AND TO_CHAR(trx_date,'MMYYYY')           = g_period_no
       AND third_party_primary_reg_num IS NOT NULL
       AND event_class_code NOT IN ('CREDIT_MEMO','DEBIT_MEMO','RECEIPTS')
       AND SUBSTR(first_party_primary_reg_num,1,15)          =  p_first_party_prim_reg_num
       AND UPPER(THIRD_PARTY_PRIMARY_REG_NUM) NOT IN ('NOT APPLICABLE')
      AND  SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,15)  =  p_third_pty_prim_reg_num
 GROUP BY  third_party_primary_reg_num ,
           trx_number,
           TRX_DATE,
           jtl.trx_id,
           JTL.PARTY_NAME,
           TAX_INVOICE_NUM ,
           TAX_INVOICE_DATE,
           first_party_primary_reg_num ;


   CURSOR b2b_item(p_first_party_prim_reg_num IN VARCHAR2,
                   p_third_pty_prim_reg_num   IN VARCHAR2,
                   p_invoice_num              IN VARCHAR2
                   )
          IS
    SELECT TAX_INVOICE_NUM,
           itm_det,
           rt,
           SUM(txval) txval,
           SUM(iamt) iamt,
           SUM(samt) samt,
           SUM(camt) camt,
           SUM(csamt) csamt,
           POS,
           INVDATE,
           CTIN
      FROM (
    SELECT NVL(TAX_INVOICE_NUM,TRX_NUMBER) TAX_INVOICE_NUM,
           JTL.PARTY_NAME       itm_det,
           (CASE WHEN  DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
           (CASE WHEN  DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) ) >=0 then ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR)  else ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) /2 end) txval,
           DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) iamt,
           DECODE (jtl.tax_type_code,'CGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) camt,
           DECODE (jtl.tax_type_code,'SGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) samt,
           DECODE (jtl.tax_type_code,'CESS', (jtl.ROUNDED_TAX_AMT_FUN_CURR))          csamt,
           SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,2)      POS,
           JTL.trx_date                                                                                   INVDATE,
           SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,15)     CTIN
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  TO_CHAR(trx_date,'MMYYYY') = g_period_no
      AND  third_party_primary_reg_num IS NOT NULL
      AND  event_class_code NOT IN ('CREDIT_MEMO','DEBIT_MEMO','RECEIPTS')
      AND  SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
      AND  NVL(TAX_INVOICE_NUM,TRX_NUMBER) = p_invoice_num
     AND   UPPER(THIRD_PARTY_PRIMARY_REG_NUM) NOT IN ('NOT APPLICABLE')
      AND  SUBSTR(REPLACE(REGEXP_REPLACE(third_party_primary_reg_num,'[^[:alnum:]'' '']', NULL),' ',''),1,15)   =  p_third_pty_prim_reg_num)
 GROUP  BY TAX_INVOICE_NUM,
           itm_det,
           rt,
           POS,
           INVDATE,
           CTIN;




   CURSOR nil(p_first_party_prim_reg_num IN VARCHAR2
              )
   IS
    SELECT sply_ty,
           SUM(line_amt) nil_amt,
           SUM(line_amt) + SUM(rounded_tax_amt_fun_curr) expt_amt,
           SUM(rounded_tax_amt_fun_curr) ngsup_amt
      FROM(
    SELECT DECODE(jtl.tax_type_code,'SGST - STD','INTRAB2B','INTRB2B') sply_ty,
           first_party_primary_reg_num gstn,
           jtl.tax_type_id ,
           jtl.tax_type_code tax_type_name,
           rounded_tax_amt_fun_curr,
           extended_amount line_amt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
           AND TO_CHAR(trx_date,'MMYYYY')  = g_period_no
           AND NVL (jtl.LIABILITY_AMOUNT, 0) = 0
           AND jtl.third_party_primary_reg_num IS NOT NULL
             AND (NVL (jtl.TAX_RATE_PERCENTAGE, jtl.ACTUAL_TAX_RATE) = 0
                        OR
                        (jtl.TAX_AMT_BEFORE_EXEMPTION > 0 AND NVL (jtl.ROUNDED_TAX_AMT_FUN_CURR, 0) = 0)
                       )
           AND jtl.EVENT_CLASS_CODE NOT IN ('CREDIT_MEMO', 'DEBIT_MEMO', 'RECEIPT')
           AND SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
                  )
  GROUP BY sply_ty
  UNION
  SELECT sply_ty,
           SUM(line_amt) nil_amt,
           SUM(line_amt) + SUM(rounded_tax_amt_fun_curr) expt_amt,
           SUM(rounded_tax_amt_fun_curr) ngsup_amt
      FROM(
    SELECT DECODE(jtl.tax_type_code,'SGST - STD','INTRAB2C','INTRB2C') sply_ty,
           first_party_primary_reg_num gstn,
           jtl.tax_type_id ,
           jtl.tax_type_code tax_type_name,
           rounded_tax_amt_fun_curr,
           extended_amount line_amt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
           AND TO_CHAR(trx_date,'MMYYYY')  = g_period_no
           AND NVL (jtl.LIABILITY_AMOUNT, 0) = 0
           AND jtl.third_party_primary_reg_num IS NULL
          AND (NVL (jtl.TAX_RATE_PERCENTAGE, jtl.ACTUAL_TAX_RATE) = 0
                        OR
                        (jtl.TAX_AMT_BEFORE_EXEMPTION > 0 AND NVL (jtl.ROUNDED_TAX_AMT_FUN_CURR, 0) = 0)
                       )
           AND jtl.EVENT_CLASS_CODE NOT IN ('CREDIT_MEMO', 'DEBIT_MEMO', 'RECEIPT')
           AND SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
                  )
  GROUP BY sply_ty;

   CURSOR export(p_first_party_prim_reg_num IN VARCHAR2
                 )
   IS
    SELECT 'WOPAY' type,
           jtl.tax_invoice_num  inum,
           jtl.trx_date   idt,
           SUM ((NVL(jtl.CURRENCY_CONVERSION_RATE,1) * jtl.line_amt)+ jtl.ROUNDED_TAX_AMT_FUN_CURR) val,
           SUBSTR(jtl.proof_of_export_num,1,7) sbnum,
           jtl.proof_received_date sbdt,
           (CASE WHEN  SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
           SUM(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) txval ,
           SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) iamt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  jtl.tax_type_code LIKE 'IGST - STD'
      AND  jtl.EVENT_CLASS_CODE NOT IN
                         ('CREDIT_MEMO', 'DEBIT_MEMO', 'RECEIPT')
      AND  jtl.tax_invoice_num IS NOT NULL
      AND (jtl.third_party_primary_reg_num IS NULL OR UPPER(THIRD_PARTY_PRIMARY_REG_NUM) IN ('NOT APPLICABLE'))
      AND  jtl.tax_rate_percentage = 0
      AND ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) >0
      AND  TO_CHAR(trx_date,'MMYYYY')   = g_period_no
      AND  NVL(jtl.self_assessed_flag,'N') <> 'Y'
      AND  SUBSTR(first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
  GROUP BY jtl.tax_invoice_num,
           jtl.trx_date ,
           jtl.proof_of_export_num,
           jtl.proof_received_date,
           jtl.tax_rate_percentage
  UNION
    SELECT 'WPAY' type,
           jtl.tax_invoice_num  inum,
           jtl.trx_date         idt,
           SUM ((NVL(jtl.CURRENCY_CONVERSION_RATE,1) * jtl.line_amt)+ jtl.ROUNDED_TAX_AMT_FUN_CURR) val,
           SUBSTR(jtl.proof_of_export_num,1,7) sbnum,
           jtl.proof_received_date sbdt,
           (CASE WHEN  SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
           SUM(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) txval ,
           SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) iamt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  jtl.tax_type_code LIKE 'IGST - STD'
      AND  jtl.EVENT_CLASS_CODE NOT IN
                         ('CREDIT_MEMO', 'DEBIT_MEMO', 'RECEIPT')
      AND  jtl.tax_invoice_num IS NOT NULL
      AND  jtl.tax_rate_percentage <> 0
      AND  TO_CHAR(trx_date,'MMYYYY')   = g_period_no
      AND  jtl.THIRD_PARTY_PRIMARY_REG_NUM IS NOT NULL
      AND  jtl.status='confirmed'
      AND  NVL(jtl.self_assessed_flag,'N') <> 'Y'
      AND  SUBSTR(first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
  GROUP BY jtl.tax_invoice_num,
           jtl.trx_date ,
           jtl.proof_of_export_num,
           jtl.proof_received_date,
           jtl.tax_rate_percentage;


   CURSOR Adv_tax(p_first_party_prim_reg_num IN VARCHAR2
                  )
   IS
    SELECT POS,
           SPLY_TY,
           RT,
           SUM(AD_AMT) AD_AMT ,
           SUM(IAMT)   IAMT,
           SUM(CAMT)   CAMT,
           SUM(SAMT)   SAMT,
           SUM(CSAMT)  CSAMT
      FROM(
    SELECT DISTINCT SUBSTR(first_party_primary_reg_num,1,2) pos,
           (case when SUBSTR(third_party_primary_reg_num,1,2) =SUBSTR(first_party_primary_reg_num,1,2)   THEN'INTRA' ELSE 'INTER' end) SPLY_TY,
           (case when  DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
            JTL.ROUNDED_TAXABLE_AMT_FUN_CURR ad_amt,
           DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) iamt,
           DECODE (jtl.tax_type_code,'CGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) camt,
           DECODE (jtl.tax_type_code,'SGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) samt,
           XXATUIND_GSTR1_JSON_PKG.get_gst_amt_fun(trx_id,'CESS') csamt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
       AND TO_CHAR(trx_date,'MMYYYY')   = g_period_no
       AND jtl.TAX_EVENT_CLASS_CODE = 'SALES_TXN_ADJUSTMENT'
       AND SUBSTR(first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
      )
  GROUP BY POS,
           SPLY_TY,
           RT;

   CURSOR TXPD( p_first_party_prim_reg_num IN VARCHAR2
            )
   IS
    SELECT POS,
           SPLY_TY,
           RT,
           SUM(AD_AMT) AD_AMT ,
           SUM(IAMT)   IAMT,
           SUM(CAMT)   CAMT,
           SUM(SAMT)   SAMT,
           SUM(CSAMT)  CSAMT
      FROM(
    SELECT DISTINCT SUBSTR(jtl.third_party_primary_reg_num,1,2) pos,
           (case when  DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) >=0 then'INTER' else 'INTRA' end) sply_ty,
           (case when  DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
           NVL(jtl.line_amt,0) ad_amt,
           DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) iamt,
           DECODE (jtl.tax_type_code,'CGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) camt,
           DECODE (jtl.tax_type_code,'SGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) samt,
           XXATUIND_GSTR1_JSON_PKG.get_gst_amt_fun(jtl.trx_id,'CESS') csamt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
       AND jtl.TAX_EVENT_CLASS_CODE = 'SALES_TXN_ADJUSTMENT'
       AND TO_CHAR(TRUNC(jtl.trx_date),'MMYYYY') LIKE TO_CHAR(ADD_MONTHS(TO_DATE(g_period_no, 'MMYYYY'), -1), 'MMYYYY')
       AND SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
      )
  GROUP BY POS,
           SPLY_TY,
            RT;

   CURSOR HSN(p_first_party_prim_reg_num IN VARCHAR2
            )
   IS
  SELECT  HSN_SC,
           UQC,
           SUM(QTY)    QTY,
           SUM(VAL)    VAL,
           SUM(TXVAL) TXVAL ,
           SUM(IAMT)   IAMT,
           SUM(CAMT)   CAMT,
           SUM(SAMT)   SAMT,
           SUM(CSAMT)  CSAMT
      FROM(
    SELECT SUBSTR(hsn_sc,1,4) hsn_sc,
           'NOS' uqc,
            NVL(DECODE(jtl.TAX_LINE_NUM,1,def_TRX_LINE_QUANTITY,0),0) qty,
         ( DECODE(jtl.TAX_LINE_NUM,1,(NVL(jtl.CURRENCY_CONVERSION_RATE,1) * jtl.line_amt) ,0) + jtl.ROUNDED_TAX_AMT_FUN_CURR) val,
         NVL(DECODE(jtl.TAX_LINE_NUM,1,jtl.ROUNDED_TAXABLE_AMT_FUN_CURR,0),0)   txval,
           DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) iamt,
           DECODE (jtl.tax_type_code,'CGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) camt,
           DECODE (jtl.tax_type_code,'SGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) samt,
           DECODE (jtl.tax_type_code,'CESS', (jtl.ROUNDED_TAX_AMT_FUN_CURR)) csamt
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  ABS(def_TRX_LINE_QUANTITY) > 0
      AND  TO_CHAR(jtl.trx_date,'MMYYYY')   = g_period_no
      AND  SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
      )
      GROUP BY  HSN_SC,
                UQC
                ;



   CURSOR CDNR_main_cur(p_first_party_prim_reg_num IN VARCHAR2
                        )
   IS
SELECT     DISTINCT NVL(jtl.third_party_primary_reg_num,'X') ctin
      FROM XXATUIND_GSTR1_TAB jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  TO_CHAR(jtl.trx_date,'MMYYYY')                = g_period_no
      AND  SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num;


   CURSOR CDNR_child_cur(p_first_party_prim_reg_num IN VARCHAR2,
                         p_in_ctin                  IN VARCHAR2
                         )
   IS
    SELECT flag,
           ctin,
           ntty,
           nt_num,
           nt_dt,
           rsn,
           p_gst,
           inum,
           idt,
           SUM(val) val,
           rt,
           SUM(txval)  txval,
           SUM(iamt)   iamt,
           SUM(camt)   camt,
           SUM(samt)   samt,
           SUM(csamt)  csamt
    FROM(SELECT DISTINCT NULL flag,SUBSTR(jtl.third_party_primary_reg_num,1,15) ctin,
                substr(jtl.event_class_code ,0,1) ntty,
                jtl.trx_number                    nt_num,
                jtl.trx_date                      nt_dt,
                '07-Others'                       rsn,
                'N'                               p_gst,
                NVL(jtl.TAX_INVOICE_NUM,jtl.trx_number) inum,
                jtl.trx_date  idt,
                ABS(LINE_AMT)+ABS(ROUNDED_TAX_AMT_FUN_CURR) val,
            (CASE WHEN jtl.tax_type_code ='IGST - STD' then  jtl.tax_rate_percentage else (jtl.tax_rate_percentage*2) end) rt,   ---added by shalini
            (CASE WHEN  DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) ) >=0 then ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR)  else ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) /2 end) txval,
                DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) iamt,
                DECODE (jtl.tax_type_code,'CGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) camt,
                DECODE (jtl.tax_type_code,'SGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) samt,
                DECODE (jtl.tax_type_code,'CESS', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) csamt
      FROM XXATUIND_GSTR1_TAB         jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
       AND TO_CHAR(jtl.trx_date,'MMYYYY')   =  g_period_no
       AND event_class_code in ('CREDIT_MEMO','DEBIT_MEMO')
       AND ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) >0
       AND SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
       AND NVL(SUBSTR(jtl.third_party_primary_reg_num,1,15),'X') = NVl(p_in_ctin,'X')
       AND jtl.third_party_primary_reg_num IS NOT NULL
         )
  GROUP BY flag,
           ctin,
           ntty,
           nt_num,
           nt_dt,
           rsn,
           p_gst,
           inum,
           idt,
            rt
           ;

   CURSOR CDNUR(p_first_party_prim_reg_num IN VARCHAR2
                 )
   IS
     SELECT flag,
            typ,
            ntty,
            nt_num,
            nt_dt,
            rsn,
            p_gst,
            inum,
            idt,
            SUM(val) val,
            rt,
            SUM(txval)  txval,
            SUM(iamt)   iamt,
            SUM(camt)   camt,
            SUM(samt)   samt,
            SUM(csamt)  csamt
    FROM(SELECT DISTINCT null flag,
                'B2CL' typ,
                substr(def_event_class_code,0,1) ntty,
                jtl.trx_number nt_num,
                jtl.trx_date nt_dt,
                '07-Others' rsn, --added by rohit
                'N' p_gst,
                NVL(JTL.TAX_INVOICE_NUM,jtl.trx_number) inum,
                jtl.trx_date idt,
                  ABS(LINE_AMT)+ABS(ROUNDED_TAX_AMT_FUN_CURR)  val,
                (CASE WHEN  DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
                (CASE WHEN  DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) ) >=0 then ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR)  else ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) /2 end) txval,
                DECODE (jtl.tax_type_code,'IGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) iamt,
                DECODE (jtl.tax_type_code,'CGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) camt,
                DECODE (jtl.tax_type_code,'SGST - STD', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) samt,
                DECODE (jtl.tax_type_code,'CESS', ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) csamt
        FROM XXATUIND_GSTR1_TAB         jtl
       WHERE jtl.entity_code LIKE 'TRANS%'
        AND TO_CHAR(jtl.trx_date,'MMYYYY')   = g_period_no
        AND def_event_class_code in ('CREDIT_MEMO','DEBIT_MEMO')
        AND ABS(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) >0
        AND jtl.third_party_primary_reg_num IS NULL
        AND SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
     )
  GROUP BY flag,
           typ,
           ntty,
           nt_num,
           nt_dt,
           rsn,
           p_gst,
           inum,
           idt,
           rt
           ;

   CURSOR DOC(p_first_party_prim_reg_num IN VARCHAR2
             )
   IS
   SELECT  def_event_class_code doc_typ,
           MIN(jtl.TAX_INVOICE_NUM) from1,
           MAX(jtl.TAX_INVOICE_NUM) to1,
           COUNT(DISTINCT jtl.tax_invoice_num)  totnum,
           0 cancel,
           COUNT(DISTINCT jtl.tax_invoice_num)-0 AS net_issue
      FROM XXATUIND_GSTR1_TAB   jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
       AND TO_CHAR(jtl.trx_date,'MMYYYY')   = g_period_no
       AND jtl.tax_invoice_num is not null
       AND SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
  GROUP BY def_event_class_code;

   CURSOR B2CL(p_first_party_prim_reg_num IN VARCHAR2
             )
   IS
    SELECT DECODE(UPPER(jtl.bill_to_state),'MAHARASHTRA','27','MADHYA PRADESH','23','TAMIL NADU','33','RAJASTHAN',
           '08','GUJARAT','24','KARNATAKA','29','UTTARAKHAND','05','Punjab','03','NEW DELHI','07')     pos,
           NULL flag,
           JTL.TAX_INVOICE_NUM inum,
           jtl.trx_date            idt,
           SUM((NVL(jtl.CURRENCY_CONVERSION_RATE,1) * ABS(jtl.line_amt))+ ABS(jtl.ROUNDED_TAX_AMT_FUN_CURR)) val,
           NULL etin  ,
           Item_description desc1,
           (CASE WHEN  SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) >=0 then nvl(jtl.tax_rate_percentage,0) else nvl((jtl.tax_rate_percentage*2),0) end) rt,
           SUM(jtl.rounded_taxable_amt_fun_curr) txval,
           SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) iamt,                      SUM(DECODE (jtl.tax_type_code,'CESS', (jtl.ROUNDED_TAX_AMT_FUN_CURR))) csamt
      FROM XXATUIND_GSTR1_TAB     jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
       AND TO_CHAR(jtl.trx_date,'MMYYYY')   = g_period_no
       AND SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
       AND (jtl.third_party_primary_reg_num IS NULL OR UPPER(THIRD_PARTY_PRIMARY_REG_NUM) IN ('NOT APPLICABLE'))
       AND (jtl.line_amt+NVL(jtl.ROUNDED_TAX_AMT_FUN_CURR,0))>250000
       AND jtl.SHIP_FROM_STATE <> jtl.BILL_TO_STATE
     GROUP BY jtl.bill_to_state,
              TAX_INVOICE_NUM,
              jtl.trx_date,
              Item_description,
              jtl.tax_rate_percentage
         ;

 CURSOR b2cs(p_first_party_prim_reg_num IN VARCHAR2
             )
   IS
    SELECT SUBSTR(first_party_primary_reg_num,1,2) pos,
           first_party_primary_reg_num,
           NULL flag,
           NULL etin  ,
           jtl.tax_rate_percentage*2 rt,
           'OE' typ,
           SUM(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR/2) txval,
           NVL(SUM(DECODE (jtl.tax_type_code,'IGST - STD',  (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) iamt,
           NVL(SUM( DECODE (jtl.tax_type_code,'CGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) camt,
           NVL(SUM(DECODE (jtl.tax_type_code,'SGST - STD',  (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) samt,
           NVL(SUM(DECODE (jtl.tax_type_code,'CESS - STD',  (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) csamt
      FROM XXATUIND_GSTR1_TAB     jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  TO_CHAR(jtl.trx_date,'MMYYYY')   = g_period_no
      AND  jtl.tax_rate_percentage  <> 0
      AND  jtl.tax_type_code in ('CGST - STD','SGST - STD')
      AND  (jtl.third_party_primary_reg_num IS NULL OR UPPER(THIRD_PARTY_PRIMARY_REG_NUM) IN ('NOT APPLICABLE'))
      AND  SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
     GROUP BY   jtl.first_party_primary_reg_num,
                jtl.tax_rate_percentage
   UNION
    SELECT DECODE(UPPER(jtl.bill_to_state),'MAHARASHTRA','27','MADHYA PRADESH','23','TAMIL NADU','33','RAJASTHAN',
           '08','GUJARAT','24','KARNATAKA','29','UTTARAKHAND','05','Punjab','03','NEW DELHI','07')     pos,
           first_party_primary_reg_num,
           NULL flag,
           NULL etin  ,
           jtl.tax_rate_percentage rt,
           'OE' typ,
           SUM(jtl.ROUNDED_TAXABLE_AMT_FUN_CURR) txval,
           NVL(SUM(DECODE (jtl.tax_type_code,'IGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) iamt,
           NVL(SUM( DECODE (jtl.tax_type_code,'CGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) camt,
           NVL(SUM(DECODE (jtl.tax_type_code,'SGST - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) samt,
           NVL(SUM(DECODE (jtl.tax_type_code,'CESS - STD', (jtl.ROUNDED_TAX_AMT_FUN_CURR))),0) csamt
      FROM XXATUIND_GSTR1_TAB     jtl
     WHERE jtl.entity_code LIKE 'TRANS%'
      AND  TO_CHAR(jtl.trx_date,'MMYYYY')   = g_period_no
      AND  jtl.tax_rate_percentage<>0
      AND  jtl.tax_type_code like 'IGST - STD'
      AND  (jtl.line_amt+NVL(jtl.ROUNDED_TAX_AMT_FUN_CURR,0))<250000
      AND  (jtl.third_party_primary_reg_num IS NULL OR UPPER(THIRD_PARTY_PRIMARY_REG_NUM) IN ('NOT APPLICABLE'))
      AND  SUBSTR(jtl.first_party_primary_reg_num,1,15)  = p_first_party_prim_reg_num
     GROUP BY jtl.first_party_primary_reg_num,
              jtl.bill_to_state,
              jtl.tax_rate_percentage;

   --Local variables
   l_num_rownum1         NUMBER;
   l_num_rownum9         NUMBER;
   l_num_rownum11        NUMBER;
   l_num_rownum12        NUMBER;
   l_num_rownum13        NUMBER;
   l_num_rownumb2cl      NUMBER;


   BEGIN
     l_num_rownum1    := 0;
     g_period_no      := p_period;

     fnd_file.put_line(fnd_file.log,'Starting main proc A ');
     fnd_file.put_line(fnd_file.log,'Period Date: '||p_period);

      FOR r_main_cur IN main_cur
      LOOP

             FOR r_ctin_b2b_extract IN ctin_b2b_extract(r_main_cur.gstn)
             LOOP

                        FOR r_b2b_inv IN b2b_inv(r_main_cur.gstn,r_ctin_b2b_extract.ctin)
                        LOOP

                             FOR r_b2b_item IN b2b_item(r_main_cur.gstn,r_b2b_inv.ctin,r_b2b_inv.inum)
                             LOOP

                         INSERT INTO XXATUIND_GSTR1_MAIN_TAB(                   DETAIL_TAX_LINE_ID
                                                                                      ,FIRST_PARTY_PRIMARY_REG_NUM
                                                                                    ,PERIOD_NAME
                                                                                     ,GROSS_TURNOVER
                                                                                     ,CURRENT_GROSS_TURNOVER
                                                                                    ,THIRD_PARTY_REG_NUM
                                                                                    ,FLAG
                                                                                    ,TAX_INVOICE_NUMBER
                                                                                    ,TAX_INVOICE_DATE
                                                                                    ,TAX_INVOICE_VALUE
                                                                                    ,STATE
                                                                                    ,REV_CHARGE
                                                                                    ,ECOM_OPERATOR
                                                                                    ,INV_TYPE
                                                                                    ,TAX_RATE
                                                                                    ,TAXABLE_AMT
                                                                                    ,IGST
                                                                                    ,CGST
                                                                                    ,SGST
                                                                                    ,CESS
                                                                                    ,SECTION_CODE
                                                                                    ,PROCESS_STATUS
                                                                                    ,SOURCE
                                                                                    ,ATTRIBUTE10
                                                                                    )
                                                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                                                    ,r_main_cur.gstn
                                                                                    ,r_main_cur.fp1
                                                                                     ,r_main_cur.gt
                                                                                     ,r_main_cur.cur_gt
                                                                                    ,r_ctin_b2b_extract.ctin
                                                                                    ,NULL
                                                                                    ,r_b2b_inv.inum
                                                                                    ,r_b2b_inv.idt
                                                                                    ,r_b2b_inv.val
                                                                                    ,r_b2b_inv.pos
                                                                                    ,r_b2b_inv.rchrg
                                                                                    ,NULL
                                                                                    ,r_b2b_inv.inv_typ
                                                                                    ,r_b2b_item.rt
                                                                                    ,r_b2b_item.txval
                                                                                    ,r_b2b_item.iamt
                                                                                    ,r_b2b_item.camt
                                                                                    ,r_b2b_item.samt
                                                                                    ,r_b2b_item.csamt
                                                                                    ,'B2B'
                                                                                    ,'I'
                                                                                    ,'ERP'
                                                                                    ,'1'
                                                                                     );

                             END LOOP;
                        END LOOP;
             END LOOP;

             COMMIT;

            FOR r_nil IN nil(r_main_cur.gstn) LOOP



                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,NIL_AMOUNT
                                                    ,EXPT_AMOUNT
                                                    ,NGSUP_AMOUNT
                                                    ,SUPP_TYPE
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,r_nil.nil_amt
                                                    ,r_nil.expt_amt
                                                    ,r_nil.ngsup_amt
                                                    ,r_nil.sply_ty
                                                    ,'NIL'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'2'
                                              );
            END LOOP;

            COMMIT;

            FOR r_export IN export(r_main_cur.gstn) LOOP


                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,EXPORT_TYPE
                                                    ,FLAG
                                                    ,TAX_INVOICE_NUMBER
                                                    ,TAX_INVOICE_DATE
                                                    ,TAX_INVOICE_VALUE
                                                    ,SHIPPABLE_BILL_NO
                                                    ,SHIPPABLE_BILL_DATE
                                                    ,TAXABLE_AMT
                                                    ,TAX_RATE
                                                    ,IGST
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                     ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,r_export.type
                                                    ,NULL
                                                    ,r_export.inum
                                                    ,r_export.idt
                                                    ,r_export.val
                                                    ,r_export.sbnum
                                                    ,r_export.sbdt
                                                    ,r_export.txval
                                                    ,r_export.rt
                                                    ,r_export.iamt
                                                    ,'EXP'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'3'
                                              );
            END LOOP;

            COMMIT;

            FOR r_at IN Adv_tax(r_main_cur.gstn) LOOP



                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,FLAG
                                                    ,STATE
                                                    ,SUPP_TYPE
                                                    ,TAX_RATE
                                                    ,ADV_RECEIPT_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SGST
                                                    ,CESS
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,NULL
                                                    ,r_at.pos
                                                    ,r_at.sply_ty
                                                    ,r_at.rt
                                                    ,r_at.ad_amt
                                                    ,r_at.iamt
                                                    ,r_at.camt
                                                    ,r_at.samt
                                                    ,r_at.csamt
                                                    ,'AT'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'4'
                                              );
            END LOOP;

            COMMIT;

            FOR r_txpd IN TXPD(r_main_cur.gstn) LOOP



                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,FLAG
                                                    ,STATE
                                                    ,SUPP_TYPE
                                                    ,TAX_RATE
                                                    ,ADV_RECEIPT_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SGST
                                                    ,CESS
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,NULL
                                                    ,r_txpd.pos
                                                    ,r_txpd.sply_ty
                                                    ,r_txpd.rt
                                                    ,r_txpd.ad_amt
                                                    ,r_txpd.iamt
                                                    ,r_txpd.camt
                                                    ,r_txpd.samt
                                                    ,r_txpd.csamt
                                                    ,'TXPD'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'5'
                                              );


            END LOOP;
              COMMIT;

            l_num_rownum9 :=0;

            FOR r_hsn IN HSN(r_main_cur.gstn) LOOP



                   l_num_rownum9  := l_num_rownum9+1;

                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,SERIAL_NO
                                                    ,HSN_CODE
                                                    ,ATTRIBUTE9
                                                    ,QTY
                                                    ,TAX_INVOICE_VALUE
                                                    ,TAXABLE_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SGST
                                                    ,CESS
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,l_num_rownum9
                                                    ,r_hsn.hsn_sc
                                                    ,r_hsn.uqc
                                                    ,r_hsn.qty
                                                    ,r_hsn.val
                                                    ,r_hsn.txval
                                                    ,r_hsn.iamt
                                                    ,r_hsn.camt
                                                    ,r_hsn.samt
                                                    ,r_hsn.csamt
                                                    ,'HSN'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'6'
                                              );
            END LOOP;

            COMMIT;

            FOR r_cdnr_main_cur IN CDNR_main_cur(r_main_cur.gstn) LOOP


                   l_num_rownum11       := 0;

                     FOR r_cdnr_child_cur IN cdnr_child_cur(r_main_cur.gstn,r_cdnr_main_cur.ctin) LOOP


                         l_num_rownum11  := l_num_rownum11+1;

                        INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,THIRD_PARTY_REG_NUM
                                                    ,FLAG
                                                    ,EVENT_CLASS_CODE
                                                    ,TRX_NUMBER
                                                    ,TRX_DATE
                                                    ,RSN
                                                    ,P_GST
                                                    ,TAX_INVOICE_NUMBER
                                                    ,TAX_INVOICE_DATE
                                                    ,TAX_INVOICE_VALUE
                                                    ,SERIAL_NO
                                                    ,TAX_RATE
                                                    ,TAXABLE_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SGST
                                                    ,CESS
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                     ,ATTRIBUTE10
                                                    )
                                            VALUES( XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,r_cdnr_child_cur.ctin
                                                    ,r_cdnr_child_cur.flag
                                                    ,r_cdnr_child_cur.ntty
                                                    ,r_cdnr_child_cur.nt_num
                                                    ,r_cdnr_child_cur.nt_dt
                                                    ,r_cdnr_child_cur.rsn
                                                    ,r_cdnr_child_cur.p_gst
                                                    ,r_cdnr_child_cur.inum
                                                    ,r_cdnr_child_cur.idt
                                                    ,r_cdnr_child_cur.val
                                                    ,l_num_rownum11
                                                    ,r_cdnr_child_cur.rt
                                                    ,r_cdnr_child_cur.txval
                                                    ,r_cdnr_child_cur.iamt
                                                    ,r_cdnr_child_cur.camt
                                                    ,r_cdnr_child_cur.samt
                                                    ,r_cdnr_child_cur.csamt
                                                    ,'CDNR'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'7'
                                              );

                   END LOOP;
            END LOOP;

            COMMIT;

            l_num_rownum12 := 0;

            FOR r_cdnur IN cdnur(r_main_cur.gstn) LOOP


                   l_num_rownum12  := l_num_rownum12+1;

                        INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                   ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,FLAG
                                                    ,EXPORT_TYPE
                                                    ,EVENT_CLASS_CODE
                                                    ,TRX_NUMBER
                                                    ,TRX_DATE
                                                    ,RSN
                                                    ,P_GST
                                                    ,TAX_INVOICE_NUMBER
                                                    ,TAX_INVOICE_DATE
                                                    ,TAX_INVOICE_VALUE
                                                    ,SERIAL_NO
                                                    ,TAX_RATE
                                                    ,TAXABLE_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SGST
                                                    ,CESS
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,r_cdnur.flag
                                                    ,r_cdnur.typ
                                                    ,r_cdnur.ntty
                                                    ,r_cdnur.nt_num
                                                    ,r_cdnur.nt_dt
                                                    ,r_cdnur.rsn
                                                    ,r_cdnur.p_gst
                                                    ,r_cdnur.inum
                                                    ,r_cdnur.idt
                                                    ,r_cdnur.val
                                                    ,l_num_rownum12
                                                    ,r_cdnur.rt
                                                    ,r_cdnur.txval
                                                    ,r_cdnur.iamt
                                                    ,r_cdnur.camt
                                                    ,r_cdnur.samt
                                                    ,r_cdnur.csamt
                                                    ,'CDNUR'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'8'
                                              );

            END LOOP;

            COMMIT;

            l_num_rownum13 :=0;

            FOR r_doc IN doc(r_main_cur.gstn) LOOP

                   l_num_rownum13  := l_num_rownum13+1;

                        INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                     ,PERIOD_NAME
                                                     ,GROSS_TURNOVER
                                                     ,CURRENT_GROSS_TURNOVER
                                                    ,FLAG
                                                    ,ATTRIBUTE1
                                                    ,ATTRIBUTE2
                                                    ,ATTRIBUTE3
                                                    ,ATTRIBUTE4
                                                    ,ATTRIBUTE5
                                                    ,ATTRIBUTE6
                                                    ,ATTRIBUTE7
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                      ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                     ,r_main_cur.gt
                                                     ,r_main_cur.cur_gt
                                                    ,NULL
                                                    ,1
                                                    ,NULL
                                                    ,r_doc.from1
                                                    ,r_doc.to1
                                                    ,r_doc.totnum
                                                    ,r_doc.cancel
                                                    ,r_doc.net_issue
                                                    ,'DOC'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'9'
                                              );

            END LOOP;

            COMMIT;

            l_num_rownumb2cl:=0;
            FOR r_b2cl IN B2CL(r_main_cur.gstn) LOOP

                     l_num_rownumb2cl:=l_num_rownumb2cl+1;

                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,STATE
                                                    ,FLAG
                                                    ,TAX_INVOICE_NUMBER
                                                    ,TAX_INVOICE_DATE
                                                    ,TAX_INVOICE_VALUE
                                                    ,ECOM_OPERATOR
                                                    ,SERIAL_NO
                                                    ,TAX_RATE
                                                    ,TAXABLE_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,NULL
                                                    ,r_b2cl.flag
                                                    ,r_b2cl.inum
                                                    ,r_b2cl.idt
                                                    ,r_b2cl.val
                                                    ,r_b2cl.etin
                                                    ,l_num_rownumb2cl
                                                    ,r_b2cl.rt
                                                    ,r_b2cl.txval
                                                    ,r_b2cl.iamt
                                                    ,r_b2cl.csamt
                                                    ,'B2CL'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'10'
                                              );

            END LOOP;


      COMMIT;

      FOR r_b2cs IN b2cs(r_main_cur.gstn) LOOP


                    INSERT INTO XXATUIND_GSTR1_MAIN_TAB(DETAIL_TAX_LINE_ID
                                                    ,FIRST_PARTY_PRIMARY_REG_NUM
                                                    ,PERIOD_NAME
                                                    ,GROSS_TURNOVER
                                                    ,CURRENT_GROSS_TURNOVER
                                                    ,STATE
                                                    ,ECOM_OPERATOR
                                                    ,inv_type
                                                    ,TAX_RATE
                                                    ,TAXABLE_AMT
                                                    ,IGST
                                                    ,CGST
                                                    ,SGST
                                                    ,CESS
                                                    ,SECTION_CODE
                                                    ,PROCESS_STATUS
                                                    ,SOURCE
                                                    ,ATTRIBUTE10
                                                    )
                                              VALUES(XXATUIND_GSTR1_SEQ.NEXTVAL
                                                    ,r_main_cur.gstn
                                                    ,r_main_cur.fp1
                                                    ,r_main_cur.gt
                                                    ,r_main_cur.cur_gt
                                                    ,r_b2cs.pos
                                                    ,r_b2cs.etin
                                                    ,r_b2cs.typ
                                                    ,r_b2cs.rt
                                                    ,r_b2cs.txval
                                                    ,r_b2cs.iamt
                                                    ,r_b2cs.camt
                                                    ,r_b2cs.samt
                                                    ,r_b2cs.csamt
                                                    ,'B2CS'
                                                    ,'I'
                                                    ,'ERP'
                                                    ,'11'
                                              );

            END LOOP;

      END LOOP;

      COMMIT;


   EXCEPTION
     WHEN OTHERS THEN
      fnd_file.put_line(fnd_file.log,'Exception occured while calling main Proc '||SQLERRM);
   END insert_ebs_data_proc;

  FUNCTION get_ctin_cnt_fun(p_first_party_prim_reg_num IN VARCHAR2,
                            p_first_party_reg_id       IN NUMBER,
                            p_batch_id                 IN NUMBER
                            ) RETURN NUMBER
  IS
   /********************************************************************************************************************
   * Type                : Function
   * Name                : get_ctin_cnt_fun
   * Input Parameters    :  1.p_first_party_prim_reg_num
   *                        2.p_first_party_reg_id
   *                        3.p_third_pty_prim_reg_num
   *                        4.p_trx_line_id
   * Purpose             : Get Item Record Count
   * Company             : Transform Edge
   * Created By          : Rohit Prasad
   * Created Date        : 16-Feb-2018
   * Last Reviewed By    :
   * Last Reviewed Date  :
   ********************************************************************************************************************
   * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
   * Date        By               Script   MKS        By                Date         Type     Details
   * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
   * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
   ********************************************************************************************************************/
  l_num_count NUMBER;
  BEGIN

  l_num_count := 0;

  SELECT COUNT(1)
    INTO l_num_count
   FROM(
   SELECT  first_party_primary_reg_num gstn,
           first_party_reg_id fp,
           third_party_primary_reg_num ctin
   FROM XXATUIND_GSTR1_TAB
   WHERE entity_code LIKE 'TRANS%'
   AND  batch_id       = p_batch_id
   AND  process_status = 'I'
   AND to_char(trx_date,'MMYYYY')  = g_period_no
   AND SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
   AND first_party_reg_id          = p_first_party_reg_id
   GROUP BY first_party_primary_reg_num,
   first_party_reg_id,
   third_party_primary_reg_num
  );

   RETURN l_num_count;

  EXCEPTION
  WHEN OTHERS THEN
   RETURN l_num_count;
  END get_ctin_cnt_fun;

  FUNCTION get_inv_rec_cnt_fun(p_first_party_prim_reg_num IN VARCHAR2,
                               p_first_party_reg_id       IN NUMBER,
                               p_third_pty_prim_reg_num   IN VARCHAR2,
                               p_batch_id                 IN NUMBER
                    ) RETURN NUMBER
   IS
   /********************************************************************************************************************
   * Type                : Function
   * Name                : get_inv_rec_cnt_fun
   * Input Parameters    :  1.p_first_party_prim_reg_num
   *                        2.p_first_party_reg_id
   *                        3.p_third_pty_prim_reg_num
   *                        4.
   * Purpose             : Get Invoice Record Count
   * Company             : Transform Edge
   * Created By          : Rohit Prasad
   * Created Date        : 16-Feb-2018
   * Last Reviewed By    :
   * Last Reviewed Date  :
   ********************************************************************************************************************
   * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
   * Date        By               Script   MKS        By                Date         Type     Details
   * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
   * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
   ********************************************************************************************************************/
   l_num_count NUMBER;
   BEGIN

   l_num_count := 0;

   SELECT COUNT(1)
   INTO   l_num_count
   FROM(
   SELECT DISTINCT trx_number,
                      trx_id,
                      trx_line_id,
                      third_party_primary_reg_num ctin,
                      tax_invoice_num inum,
                      TO_CHAR(last_update_date,'DD/MM/YYYY') idt,
                      (CASE WHEN tax_currency_code='USD' then (CURRENCY_CONVERSION_RATE*line_amt) else line_amt end) iamt,
                      rounded_taxable_amt_fun_curr     val,
                      NVL (jtl.tax_rate_uom, 06) pos,
                      'N' rchrg, --Need to change
                      'R' inv_typ
   FROM  XXATUIND_GSTR1_TAB jtl
   WHERE jtl.entity_code LIKE 'TRANS%'
    AND  batch_id       = p_batch_id
   AND  process_status = 'I'
   AND to_char(trx_date,'MMYYYY') = g_period_no
   AND SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
   AND first_party_reg_id          = p_first_party_reg_id
   AND NVl(SUBSTR(third_party_primary_reg_num,1,15),'X') = NVL(p_third_pty_prim_reg_num,'X')
    );

    RETURN l_num_count;

   EXCEPTION
   WHEN OTHERS THEN
    RETURN l_num_count;
   END get_inv_rec_cnt_fun;

  FUNCTION get_item_rec_cnt_fun(p_first_party_prim_reg_num   IN VARCHAR2,
                                p_first_party_reg_id         IN NUMBER,
                                p_third_pty_prim_reg_num     IN VARCHAR2,
                                p_trx_line_id                IN NUMBER
                               ) RETURN NUMBER
  IS
   /********************************************************************************************************************
   * Type                : Function
   * Name                : get_item_rec_cnt_fun
   * Input Parameters    :  1.p_first_party_prim_reg_num
   *                        2.p_first_party_reg_id
   *                        3.p_third_pty_prim_reg_num
   *                        4.p_trx_line_id
   * Purpose             : Get Item Record Count
   * Company             : Transform Edge
   * Created By          : Rohit Prasad
   * Created Date        : 16-Feb-2018
   * Last Reviewed By    :
   * Last Reviewed Date  :
   ********************************************************************************************************************
   * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
   * Date        By               Script   MKS        By                Date         Type     Details
   * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
   * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
   ********************************************************************************************************************/
  l_num_count NUMBER;
  BEGIN

  l_num_count := 0;

  SELECT COUNT(1)
    INTO l_num_count
   FROM(
  SELECT trx_number,
         trx_id,
         jtl.tax_line_num num,
         (SELECT msi.description
            FROM  mtl_system_items_b msi
            WHERE 1=1
            AND msi.organization_id   = jtl.organization_id
           AND msi.inventory_item_id = jtl.item_id ) itm_det,
         SUM(jtl.tax_rate_percentage) OVER (PARTITION BY jtl.trx_number)  rt,
         --18 rt,
         jtl.rounded_taxable_amt_fun_curr txval,
         (CASE WHEN tax_currency_code='USD' then (CURRENCY_CONVERSION_RATE*line_amt) else line_amt end) iamt,
         XXATUIND_GSTR1_JSON_PKG.get_gst_amt_fun(jtl.trx_id,'CESS')    csamt
   FROM XXATUIND_GSTR1_TAB jtl
  WHERE jtl.entity_code LIKE 'TRANS%'
     AND to_char(trx_date,'MMYYYY') = g_period_no
    AND SUBSTR(first_party_primary_reg_num,1,15) = p_first_party_prim_reg_num
    AND first_party_reg_id          = p_first_party_reg_id
    AND NVl(third_party_primary_reg_num,'X') = NVL(p_third_pty_prim_reg_num,'X')
    AND trx_line_id      = p_trx_line_id
  );

   RETURN l_num_count;

  EXCEPTION
  WHEN OTHERS THEN
   RETURN l_num_count;
  END get_item_rec_cnt_fun;



--   PROCEDURE get_token_proc
--   /********************************************************************************************************************
--   * Type                : Procedure
--   * Name                : get_token_proc
--   * Input Parameters    :  1.p_period


--   *                        2.
--   *
--   * Purpose             : Generate Token
--   * Company             : Transform Edge
--   * Created By          : Rohit Prasad
--   * Created Date        : 16-Feb-2018
--   * Last Reviewed By    :
--   * Last Reviewed Date  :
--   ********************************************************************************************************************
--   * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->

--   * Date        By               Script   MKS        By                Date         Type     Details
--   * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
--   * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
--   ********************************************************************************************************************/

--   IS
--       v_request         UTL_HTTP.req;
--       gv_access_token   UTL_HTTP.resp;
--       --v_text          clob;
--       v_text            VARCHAR2(1000);
--       v_all_text        varchar2(3000);





--
--       ln_token_len number;










































--   BEGIN






































--     fnd_file.put_line(fnd_file.log,'Starting get_token_proc...');
--
--     v_text:= 'data:{}';

--
--     v_request := UTL_HTTP.begin_request('https://api.wepgst.com/api/token/getToken/', 'POST');

--
--     UTL_HTTP.set_header (v_request,
--                         'Ocp-Apim-Subscription-Key',

--                         '7ad84a349ab045e7a2e0f274f17a8855');
--     UTL_HTTP.set_header (v_request,


--                         'client_id',
--                         '020ee96f-147d-4e0c-b730-348c5a4f0219');
--     UTL_HTTP.set_header (v_request,
--                         'client_secret',


--                         'Ykeh6cgGTxkOiiO60Rb69VWILR78Iu+mGz1Aa1DKDFQ=');
--
--     UTL_HTTP.set_header(v_request,
--                        'Content-Length',
--                        LENGTH(v_text));

--
--     UTL_HTTP.write_text(v_request, v_text);

--
--     gv_access_token := UTL_HTTP.get_response(v_request);

--
--     fnd_file.put_line(fnd_file.log,RPAD('=', 80, '='));
--     fnd_file.put_line(fnd_file.log,'Response status code: ' || gv_access_token.status_code);
--     fnd_file.put_line(fnd_file.log,'Response reason phrase: ' || gv_access_token.reason_phrase);

--
--     v_all_text := NULL;

--
--     LOOP
--         BEGIN
--             UTL_HTTP.read_text(gv_access_token, v_text);

--
--             v_all_text := v_all_text || v_text;

--
--         EXCEPTION
--             WHEN UTL_HTTP.end_of_body




--             THEN
--                 NULL;
--         END;
--
--         EXIT WHEN v_text IS NULL;
--     END LOOP;


--
--
--     ln_token_len := LENGTH(v_all_text)-199;
--     gv_token     := 'Bearer ' || SUBSTR(v_all_text,198,ln_token_len);


--
--
--     fnd_file.put_line(fnd_file.log,RPAD('=', 80, '='));

--
--     UTL_HTTP.end_response(gv_access_token);
--   EXCEPTION
--    WHEN OTHERS THEN
--      fnd_file.put_line(fnd_file.log,'Exception occured while calling get_token_proc Proc '||SQLERRM);
--   END get_token_proc;

--
--  PROCEDURE gstr1_save_proc(p_json_data       IN VARCHAR2,

--                            p_period          IN VARCHAR2,
--                            p_batch_id        IN NUMBER,

--                            p_gstin           IN VARCHAR2)
--   IS
--   /********************************************************************************************************************
--   * Type                : Procedure
--   * Name                : gstr1_save_proc
--   * Input Parameters    : p_json_data



--   *                     : p_period
--   *                     : p_batch_id
--   *                     : p_gstin
--   * Purpose             : Generate Token
--   * Company             : Transform Edge
--   * Created By          : Rohit Prasad
--   * Created Date        : 16-Feb-2018
--   * Last Reviewed By    :
--   * Last Reviewed Date  :
--   ********************************************************************************************************************
--   * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->

--   * Date        By               Script   MKS        By                Date         Type     Details
--   * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
--   * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
--   ********************************************************************************************************************/
--    v_request     UTL_HTTP.req;
--    v_response    UTL_HTTP.resp;
--    ln_token_len  NUMBER;
--    v_text        VARCHAR2(32000);












--
--
--BEGIN


































































--
--
--
--   fnd_file.put_line(fnd_file.log,'Starting gstr1_save_proc...');
--   v_text:= p_json_data;


--
--
--    v_request := UTL_HTTP.begin_request('https://api.wepgst.com/asp/gstr1/save/', 'POST');
--    UTL_HTTP.set_header (v_request,
--                        'Ocp-Apim-Subscription-Key',


--                        '7ad84a349ab045e7a2e0f274f17a8855'
--                       );
--   UTL_HTTP.set_header (v_request,
--                        'Authorization',


--                         gv_token
--                       );
--   UTL_HTTP.set_header (v_request,




--                        'GSTINNO ',
--                        '29AAKCA7120L1ZE'
--                       );
--
--   UTL_HTTP.set_header (v_request,
--                        'SOURCE_TYPE',


--                        'ERP'
--                       );
--   UTL_HTTP.set_header (v_request,
--                        'REFERENCE_NO   ',



--                        'WEP00531'
--                       );
--
--   UTL_HTTP.set_header (v_request,
--                      'Content-Type',
--                      'application/json'


--                       );
--
--   UTL_HTTP.set_header(v_request,
--                       'Content-Length',
--                       LENGTH(v_text));

--
--    UTL_HTTP.set_header(v_request,
--                       'OUTPUTTYPE',



--                       'XML' );
--
--
--    UTL_HTTP.write_text(v_request, v_text);

--
--    v_response := UTL_HTTP.get_response(v_request);

--
--    fnd_file.put_line(fnd_file.log,RPAD('=', 80, '='));
--    fnd_file.put_line(fnd_file.log,'Response status code: ' || v_response.status_code);
--    fnd_file.put_line(fnd_file.log,'Response reason phrase: ' || v_response.reason_phrase);

--
--    LOOP
--        BEGIN
--            UTL_HTTP.read_text(v_response, v_text);
--            fnd_file.put_line(fnd_file.log,v_text);

--
--        --Calling  XML_RESPONSE_UPDATE_PROC procedure to update response in stage table


--
--
--         XML_RESPONSE_UPDATE_PROC(P_BATCH_ID => p_batch_id,

--                                  P_PERIOD => p_period,
--                                  P_FIRST_PARTY_REG_NUM =>p_gstin,
--                                  P_XML_RESP =>v_text



--                                  ) ;
--
--
--        EXCEPTION
--            WHEN UTL_HTTP.end_of_body




--            THEN
--                NULL;
--        END;
--
--        EXIT WHEN v_text IS NULL;
--    END LOOP;

--
--    fnd_file.put_line(fnd_file.log,RPAD('=', 80, '='));

--
--    UTL_HTTP.end_response(v_response);
--  END gstr1_save_proc;

   PROCEDURE main_proc(p_ret_message   OUT VARCHAR2,
                       p_ret_code      OUT NUMBER,
                       p_period         IN VARCHAR2)
   AS
    /********************************************************************************************************************
    * Type                : Procedure
    * Name                : main_proc
    * Input Parameters    :  1.p_period
    *
    * Purpose             : Generate GST1 Data like Json format
    * Company             : Transform Edge
    * Created By          : Rohit Prasad
    * Created Date        : 16-Feb-2018
    * Last Reviewed By    :
    * Last Reviewed Date  :
    ********************************************************************************************************************
    * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
    * Date        By               Script   MKS        By                Date         Type     Details
    * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
    * 16-Feb-2018 Rohit Prasad       0.00                                                I      Initial Version
    ********************************************************************************************************************/

    CURSOR ctin_b2b_extract
    IS
    SELECT DISTINCT batch_id,first_party_primary_reg_num
    FROM  XXATUIND_GSTR1_MAIN_TAB  xgt
    WHERE xgt.process_status           = 'I'
      AND PERIOD_NAME                  = p_period
    GROUP BY batch_id,first_party_primary_reg_num
      ORDER BY 1;

     l_chr_message varchar2(32000);
     l_num_status  NUMBER;
      v_conc_req_id       NUMBER;
    BEGIN

    v_conc_req_id:=fnd_global.conc_request_id;

     fnd_file.put_line(fnd_file.log,'Starting main proc1 ');

     fnd_file.put_line(fnd_file.log,'Period Date1: '||p_period);


        insert_gstr1_proc(p_period);


 fnd_file.put_line(fnd_file.log,'Period Date2: '||p_period);
          insert_ebs_data_proc(    p_period
                                  );


        create_main_batch_proc(p_period);


           FOR r_ctin_b2b_extract IN ctin_b2b_extract
           LOOP

             l_chr_message := NULL;
             l_num_status  := NULL;
             fnd_file.put_line(fnd_file.log,'Batch number:'||r_ctin_b2b_extract.batch_id);

              process_jason_format_proc(p_ret_message => l_chr_message,
                                        p_ret_code    => l_num_status,
                                        p_period      => p_period,
                                        p_batch_id    => r_ctin_b2b_extract.batch_id,
                                        p_first_party_prim_reg_num    => r_ctin_b2b_extract.first_party_primary_reg_num
                                       );
         BEGIN
             IF l_chr_message IS NULL
             THEN
                 UPDATE XXATUIND_GSTR1_MAIN_TAB  xgt
                   SET  xgt.process_status   = 'P'
                 WHERE 1=1
                 AND xgt.batch_id = r_ctin_b2b_extract.batch_id
                 AND xgt.first_party_primary_reg_num = r_ctin_b2b_extract.first_party_primary_reg_num
                 AND period_name  = p_period
                 ;

             ELSE

                 UPDATE XXATUIND_GSTR1_MAIN_TAB  xgt
                   SET  xgt.process_status   = 'E'
                 WHERE 1=1
                 AND xgt.batch_id  = r_ctin_b2b_extract.batch_id
                  AND xgt.first_party_primary_reg_num = r_ctin_b2b_extract.first_party_primary_reg_num
                 AND period_name   = p_period
                 ;

             END IF;

            COMMIT;
            EXCEPTION
    WHEN OTHERS THEN
        fnd_file.put_line(fnd_file.log,'Exception occured while updating XXATUIND_GSTR1_MAIN_TAB'||SQLERRM);
        end;

           END LOOP;

--Calling CALL_REPORT_PROC     to run Report Program

CALL_REPORT_PROC(p_request_id => v_conc_req_id);

    EXCEPTION
    WHEN OTHERS THEN
        fnd_file.put_line(fnd_file.log,'Exception occured while calling main_proc Proc '||SQLERRM);
    END main_proc;


      PROCEDURE create_main_batch_proc(p_period   IN VARCHAR2)
   AS
    /********************************************************************************************************************
    * Type                : Procedure
    * Name                : create_main_batch_proc
    * Input Parameters    :  1.p_period
    *
    * Purpose             : create batch to divide records based on batch_id
    * Company             : Transform Edge
    * Created By          : Venkatesh C
    * Created Date        : 21-Jul-2016
    * Last Reviewed By    :
    * Last Reviewed Date  :
    ********************************************************************************************************************
    * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
    * Date        By               Script   MKS        By                Date         Type     Details
    * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
    * 15-Oct-2017 Venkatesh C       0.00                                                I      Initial Version
    ********************************************************************************************************************/

   CURSOR c1
    IS
    SELECT DISTINCT FIRST_PARTY_PRIMARY_REG_NUM,ATTRIBUTE10
      FROM XXATUIND_GSTR1_MAIN_TAB xgmt
    WHERE  xgmt.PERIOD_NAME        = p_period
      AND process_status  = 'I'
     ORDER BY FIRST_PARTY_PRIMARY_REG_NUM
       ;


     CURSOR C2
     IS
     SELECT DISTINCT section_code
       FROM XXATUIND_GSTR1_MAIN_TAB
      WHERE PERIOD_NAME        = p_period
      AND process_status  = 'I'
      ORDER BY 1;



        Cursor C3(P_FIRST_PARTY_REG_NUM IN VARCHAR2,
                  P_SOURCE_SECTION      IN VARCHAR2,
                  P_ID  IN NUMBER

                  )
    IS
   SELECT ceil(rownum/20) batch_id,
          a.first_party_primary_reg_num,
          a.THIRD_PARTY_REG_NUM,
          a.TAX_INVOICE_NUMBER,
          a.hsn_code
    FROM(
    SELECT FIRST_PARTY_PRIMARY_REG_NUM,
           THIRD_PARTY_REG_NUM,
           TAX_INVOICE_NUMBER,
           hsn_code
    FROM XXATUIND_GSTR1_MAIN_TAB
   WHERE FIRST_PARTY_PRIMARY_REG_NUM = P_FIRST_PARTY_REG_NUM
   AND   section_code              = P_SOURCE_SECTION
   AND   ATTRIBUTE10   =     P_ID
   AND   NVL(process_status,'I')     =  'I'
   AND   PERIOD_NAME                 = p_period
    AND  NVL(process_status,'I')     =  'I'
    GROUP BY first_party_primary_reg_num,
             THIRD_PARTY_REG_NUM,
             TAX_INVOICE_NUMBER,
             hsn_code
    ORDER BY 1,2
    ) a;




     l_batch_id NUMBER;
    BEGIN

    fnd_file.put_line(fnd_file.log,'Inside Loop: '||p_period);

    l_batch_id :=0;



      FOR I IN C1 LOOP

      SELECT NVL(MAX(batch_id),0)
          INTO l_batch_id
      FROM XXATUIND_GSTR1_MAIN_TAB xgmt
      WHERE xgmt.PERIOD_NAME        = p_period
       AND NVL(process_status,'I')  = 'I';

          FOR J IN C2 LOOP

            FOR K IN C3(I.first_party_primary_reg_num,
                        J.section_code,
                        I.ATTRIBUTE10
                        )
            LOOP

           UPDATE XXATUIND_GSTR1_MAIN_TAB  xgt
           SET xgt.batch_id         = l_batch_id+K.batch_id,
               xgt.process_status   = 'I'
            WHERE 1=1
            AND xgt.first_party_primary_reg_num  =   K.first_party_primary_reg_num
            AND NVL(xgt.THIRD_PARTY_REG_NUM,'X')            = NVL(K.THIRD_PARTY_REG_NUM,'X')
            AND NVL(xgt.TAX_INVOICE_NUMBER,'X')             = NVL(K.TAX_INVOICE_NUMBER,'X')
            AND NVL(xgt.hsn_code,0)                         = NVL(K.hsn_code,0)
            AND period_name                        =   p_period
            AND ATTRIBUTE10                         =   I.ATTRIBUTE10
            AND section_code                       =   J.section_code
            AND batch_id     IS NULL
            ;


            END LOOP;

          COMMIT;


      END LOOP;

      END LOOP;

    EXCEPTION
    WHEN OTHERS THEN
        fnd_file.put_line(fnd_file.log,'Exception occured while calling create_main_batch_proc Proc '||SQLERRM);
    END create_main_batch_proc;



   FUNCTION get_gst_amt_fun (p_trx_id     NUMBER,
                             p_tax_type   VARCHAR2
                            )
      RETURN NUMBER
   IS
      v_tax_amt       NUMBER;

      CURSOR c_tax_amt
      IS
         SELECT SUM(nvl(rounded_tax_amt_fun_curr,0))
           FROM XXATUIND_GSTR1_TAB jtl
          WHERE jtl.trx_id        = p_trx_id
            AND entity_code LIKE 'TRANS%'
            AND tax_type_code = p_tax_type;
   BEGIN
      v_tax_amt     := 0;

      OPEN  c_tax_amt;
      FETCH c_tax_amt INTO v_tax_amt;
      CLOSE c_tax_amt;

        RETURN NVL(v_tax_amt, 0);

   EXCEPTION
      WHEN OTHERS
      THEN
         DBMS_OUTPUT.put_line ('Exception at Total ALL GGT Amount Function');
   END get_gst_amt_fun;

    FUNCTION get_tax_invoice_value (p_trx_id      NUMBER,
                                   p_det_factor_id    NUMBER
                                    )
      RETURN NUMBER
   IS
      ln_tax_invoice_amt   NUMBER;
   BEGIN

      SELECT ( (SELECT SUM (line_amt)
                  FROM jai_tax_det_factors
                 WHERE trx_id = p_trx_id
                 AND det_factor_id =
                              NVL (p_det_factor_id, det_factor_id)
                   AND entity_code like 'TRANS%'
                   )
              + (SELECT  SUM (NVL(ROUNDED_TAX_AMT_FUN_CURR,0))
                   FROM jai_tax_lines a
                  WHERE a.trx_id = p_trx_id
                  AND det_factor_id =
                               NVL (p_det_factor_id, det_factor_id)
                   AND a.entity_code like 'TRANS%'
                        )
                        )
                Tax_Invoice_Amount
        INTO ln_tax_invoice_amt
        FROM DUAL;

      RETURN ln_tax_invoice_amt;
   EXCEPTION
      WHEN OTHERS
      THEN
         ln_tax_invoice_amt := 0;
         RETURN ln_tax_invoice_amt;
   END get_tax_invoice_value;

   FUNCTION get_adjustment_amount (p_trx_id      NUMBER,
                                   p_trx_line_id NUMBER)
      RETURN NUMBER
   IS
      ln_tax_invoice_amt   NUMBER;
   BEGIN
      SELECT (SELECT SUM (NVL (liability_amount, 0))
            FROM XXATUIND_GSTR1_TAB jrrl
           WHERE def_event_class_code = 'SALES_TXN_ADJUSTMENT'
             AND jrrl.trx_id = p_trx_id
             AND jrrl.trx_line_id = p_trx_line_id)
                Tax_Invoice_Amount
        INTO ln_tax_invoice_amt
        FROM DUAL;

      RETURN ln_tax_invoice_amt;
   EXCEPTION
      WHEN OTHERS
      THEN
         ln_tax_invoice_amt := 0;
         RETURN ln_tax_invoice_amt;
   END get_adjustment_amount;

   PROCEDURE insert_gstr1_proc(p_period   IN VARCHAR2)
   AS
    /********************************************************************************************************************
    * Type                : Procedure
    * Name                : insert_gstr1_proc
    * Input Parameters    : 1.p_period
    *
    * Purpose             : Insert GST1 Data into
    * Company             : Transform Edge
    * Created By          : Rohit Prasad
    * Created Date        : 21-Dec-2017
    * Last Reviewed By    :
    * Last Reviewed Date  :
    ********************************************************************************************************************
    * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification --------->
    * Date        By               Script   MKS        By                Date         Type     Details
    * ----------- ---------------- -------- ---------- ----------------- ----------- ---- -----------------------------
    * 21-Dec-2017 Rohit Prasad       0.00                                                I      Initial Version
    ********************************************************************************************************************/
   BEGIN

        INSERT INTO XXATUIND_GSTR1_TAB(TAX_LINE_ID,
                            DET_FACTOR_ID,
                            ORG_ID,
                            ORGANIZATION_ID,
                            LOCATION_ID,
                            APPLICATION_ID,
                            ENTITY_CODE,
                            EVENT_CLASS_CODE,
                            EVENT_TYPE_CODE,
                            TAX_EVENT_CLASS_CODE,
                            TAX_EVENT_TYPE_CODE,
                            LEDGER_ID,
                            TRX_ID,
                            TRX_LINE_ID,
                            TRX_LOC_LINE_ID,
                            TRX_LEVEL_TYPE,
                            TRX_NUMBER,
                            TRX_LINE_NUMBER,
                            TRX_TYPE,
                            TRX_DATE,
                            LEGAL_ENTITY_ID,
                            FROZEN_FLAG,
                            PARTY_TYPE,
                            PARTY_ID,
                            PARTY_NAME,
                            PARTY_SITE_ID,
                            PARTY_SITE_NAME,
                            TRX_UOM,
                            ITEM_ID,
                            UNIT_PRICE,
                            LINE_AMT,
                            TRX_LINE_QUANTITY,
                            TRX_CURRENCY_CODE,
                            CURRENCY_CONVERSION_DATE,
                            CURRENCY_CONVERSION_TYPE,
                            CURRENCY_CONVERSION_RATE,
                            TAX_CURRENCY_CODE,
                            TAX_CURRENCY_CONVERSION_DATE,
                            TAX_CURRENCY_CONVERSION_TYPE,
                            TAX_CURRENCY_CONVERSION_RATE,
                            FUNCTIONAL_CURRENCY_CODE,
                            LINE_ASSESSABLE_VALUE,
                            TAX_REGIME_ID,
                            TAX_REGIME_CODE,
                            TAX_REGIME_TYPE,
                            FIRST_PARTY_REG_ID,
                            FIRST_PARTY_PRIMARY_REG_NAME,
                            FIRST_PARTY_PRIMARY_REG_NUM,
                            FIRST_PARTY_SECONDARY_REG_NAME,
                            FIRST_PARTY_SECONDARY_REG_NUM,
                            NUM_OF_RETURN_DAYS,
                            REPORTING_ONLY_FLAG,
                            TAX_AUTHORITY_ID,
                            TAX_AUTHORITY_SITE_ID,
                            THIRD_PARTY_REG_ID,
                            THIRD_PARTY_PRIMARY_REG_NAME,
                            THIRD_PARTY_PRIMARY_REG_NUM,
                            THIRD_PARTY_SECONDARY_REG_NAME,
                            THIRD_PARTY_SECONDARY_REG_NUM,
                            TAX_TYPE_ID,
                            RECOVERABLE_FLAG,
                            SELF_ASSESSED_FLAG,
                            TAX_POINT_BASIS,
                            TAX_POINT_DATE,
                            TAX_RATE_ID,
                            TAX_RATE_CODE,
                            TAX_RATE_TYPE,
                            TAX_STATUS,
                            RECOVERY_PERCENTAGE,
                            TAX_RATE_PERCENTAGE,
                            TAX_RATE_UOM,
                            TAX_RATE_UOM_RATE,
                            TAX_RATE_ABATEMENT_TYPE,
                            ABATEMENT_PERCENTAGE,
                            TAX_RATE_CLASSIFICATION,
                            STANDARD_RATE,
                            FORM_TYPE,
                            ACTUAL_TAX_RATE,
                            ROUNDING_LEVEL,
                            TAX_ROUNDED_TO,
                            TAX_ROUNDING_FACTOR,
                            TAXABLE_ROUNDED_TO,
                            TAXABLE_ROUNDING_FACTOR,
                            QUANTITY_ROUNDED_TO,
                            QUANTITY_ROUNDING_FACTOR,
                            UNROUND_TAXABLE_AMT_TRX_CURR,
                            UNROUND_TAXABLE_AMT_TAX_CURR,
                            UNROUND_TAXABLE_AMT_FUN_CURR,
                            UNROUND_TAX_AMT_TRX_CURR,
                            UNROUND_TAX_AMT_TAX_CURR,
                            UNROUND_TAX_AMT_FUN_CURR,
                            UNROUNDED_UOM_CONV_QTY,
                            ROUNDED_TAX_AMT_TRX_CURR,
                            ROUNDED_TAX_AMT_TAX_CURR,
                            ROUNDED_TAX_AMT_FUN_CURR,
                            ROUNDED_TAXABLE_AMT_TRX_CURR,
                            ROUNDED_TAXABLE_AMT_TAX_CURR,
                            ROUNDED_TAXABLE_AMT_FUN_CURR,
                            ROUNDED_QUANTITY,
                            REC_TAX_AMT_TRX_CURR,
                            REC_TAX_AMT_TAX_CURR,
                            REC_TAX_AMT_FUNCL_CURR,
                            NREC_TAX_AMT_TRX_CURR,
                            NREC_TAX_AMT_TAX_CURR,
                            NREC_TAX_AMT_FUNCL_CURR,
                            ORIGINAL_TAX_AMT,
                            ENCUMBERANCE_NR_TAX_AMT,
                            ENCUMBERANCE_NR_FUNC_TAX_AMT,
                            ENCUMBERANCE_STATUS_FLAG,
                            TAX_LINE_NUM,
                            PRECEDENCE_1,
                            PRECEDENCE_2,
                            PRECEDENCE_3,
                            PRECEDENCE_4,
                            PRECEDENCE_5,
                            PRECEDENCE_6,
                            PRECEDENCE_7,
                            PRECEDENCE_8,
                            PRECEDENCE_9,
                            PRECEDENCE_10,
                            PRECEDENCE_11,
                            PRECEDENCE_12,
                            PRECEDENCE_13,
                            PRECEDENCE_14,
                            PRECEDENCE_15,
                            PRECEDENCE_16,
                            PRECEDENCE_17,
                            PRECEDENCE_18,
                            PRECEDENCE_19,
                            PRECEDENCE_20,
                            TAX_INVOICE_DATE,
                            TAX_INVOICE_NUM,
                            RECORD_TYPE_CODE,
                            CREATION_DATE,
                            CREATED_BY,
                            LAST_UPDATE_DATE,
                            LAST_UPDATED_BY,
                            LAST_UPDATE_LOGIN,
                            OBJECT_VERSION_NUMBER,
                            USER_ENTERED_AV,
                            TAXABLE_BASIS,
                            EXEMPTION_TYPE,
                            EXEMPTION_NUM,
                            TRACKING_NUM,
                            EXEMPTION_DATE,
                            PROOF_OF_EXPORT_NUM,
                            PROOF_RECEIVED_DATE,
                            EXEMPTION_HDR_ID,
                            TAX_AMT_BEFORE_EXEMPTION,
                            THIRD_PARTY_PROC_FLAG,
                            TP_STANDARD_INV_NUM,
                            TP_CREDIT_INV_NUM,
                            RETROACTIVE_FLAG,
                            STATUS,
                            ELIGIBLE_GSTR1,
                            STATUS_GSTR1,
                            ELIGIBLE_GSTR2,
                            STATUS_GSTR2,
                            ERROR_MSG_GSTR1,
                            ERROR_MSG_GSTR2,
                            ERROR_MSG_CODE1,
                            ERROR_MSG_CODE2,
                            SOURCE,
                            SOURCE_APPLICATION,
                            PROCESS_STATUS,
                            BATCH_ID, -------
                            Item_description,
                            tax_type_code,
                            extended_amount,
                            def_TRX_UOM_CODE,
                            def_TRX_LINE_QUANTITY,
                            def_event_class_code,
                            def_trx_type,
                            liability_amount,
                            hsn_sc,
                            bill_to_state,
                            ship_to_state,
                            ship_from_state
                            )
                   SELECT   jtl.TAX_LINE_ID,
                            jtl.DET_FACTOR_ID,
                            jtl.ORG_ID,
                            jtl.ORGANIZATION_ID,
                            jtl.LOCATION_ID,
                            jtl.APPLICATION_ID,
                            jtl.ENTITY_CODE,
                            jtl.EVENT_CLASS_CODE,
                            jtl.EVENT_TYPE_CODE,
                            jtl.TAX_EVENT_CLASS_CODE,
                            jtl.TAX_EVENT_TYPE_CODE,
                            jtl.LEDGER_ID,
                            jtl.TRX_ID,
                            jtl.TRX_LINE_ID,
                            jtl.TRX_LOC_LINE_ID,
                            jtl.TRX_LEVEL_TYPE,
                            jtl.TRX_NUMBER,
                            jtl.TRX_LINE_NUMBER,
                            jtl.TRX_TYPE,
                            jtl.TRX_DATE,
                            jtl.LEGAL_ENTITY_ID,
                            jtl.FROZEN_FLAG,
                            jtl.PARTY_TYPE,
                            jtl.PARTY_ID,
                            jtl.PARTY_NAME,
                            jtl.PARTY_SITE_ID,
                            jtl.PARTY_SITE_NAME,
                            jtl.TRX_UOM,
                            jtl.ITEM_ID,
                            jtl.UNIT_PRICE,
                            jtl.LINE_AMT,
                            jtl.TRX_LINE_QUANTITY,
                            jtl.TRX_CURRENCY_CODE,
                            jtl.CURRENCY_CONVERSION_DATE,
                            jtl.CURRENCY_CONVERSION_TYPE,
                            jtl.CURRENCY_CONVERSION_RATE,
                            jtl.TAX_CURRENCY_CODE,
                            jtl.TAX_CURRENCY_CONVERSION_DATE,
                            jtl.TAX_CURRENCY_CONVERSION_TYPE,
                            jtl.TAX_CURRENCY_CONVERSION_RATE,
                            jtl.FUNCTIONAL_CURRENCY_CODE,
                            jtl.LINE_ASSESSABLE_VALUE,
                            jtl.TAX_REGIME_ID,
                            jtl.TAX_REGIME_CODE,
                            jtl.TAX_REGIME_TYPE,
                            jtl.FIRST_PARTY_REG_ID,
                            jtl.FIRST_PARTY_PRIMARY_REG_NAME,
                            jtl.FIRST_PARTY_PRIMARY_REG_NUM,
                            jtl.FIRST_PARTY_SECONDARY_REG_NAME,
                            jtl.FIRST_PARTY_SECONDARY_REG_NUM,
                            jtl.NUM_OF_RETURN_DAYS,
                            jtl.REPORTING_ONLY_FLAG,
                            jtl.TAX_AUTHORITY_ID,
                            jtl.TAX_AUTHORITY_SITE_ID,
                            jtl.THIRD_PARTY_REG_ID,
                            jtl.THIRD_PARTY_PRIMARY_REG_NAME,
                            jtl.THIRD_PARTY_PRIMARY_REG_NUM,
                            jtl.THIRD_PARTY_SECONDARY_REG_NAME,
                            jtl.THIRD_PARTY_SECONDARY_REG_NUM,
                            jtl.TAX_TYPE_ID,
                            jtl.RECOVERABLE_FLAG,
                            jtl.SELF_ASSESSED_FLAG,
                            jtl.TAX_POINT_BASIS,
                            jtl.TAX_POINT_DATE,
                            jtl.TAX_RATE_ID,
                            jtl.TAX_RATE_CODE,
                            jtl.TAX_RATE_TYPE,
                            jtl.TAX_STATUS,
                            jtl.RECOVERY_PERCENTAGE,
                            jtl.TAX_RATE_PERCENTAGE,
                            jtl.TAX_RATE_UOM,
                            jtl.TAX_RATE_UOM_RATE,
                            jtl.TAX_RATE_ABATEMENT_TYPE,
                            jtl.ABATEMENT_PERCENTAGE,
                            jtl.TAX_RATE_CLASSIFICATION,
                            jtl.STANDARD_RATE,
                            jtl.FORM_TYPE,
                            jtl.ACTUAL_TAX_RATE,
                            jtl.ROUNDING_LEVEL,
                            jtl.TAX_ROUNDED_TO,
                            jtl.TAX_ROUNDING_FACTOR,
                            jtl.TAXABLE_ROUNDED_TO,
                            jtl.TAXABLE_ROUNDING_FACTOR,
                            jtl.QUANTITY_ROUNDED_TO,
                            jtl.QUANTITY_ROUNDING_FACTOR,
                            jtl.UNROUND_TAXABLE_AMT_TRX_CURR,
                            jtl.UNROUND_TAXABLE_AMT_TAX_CURR,
                            jtl.UNROUND_TAXABLE_AMT_FUN_CURR,
                            jtl.UNROUND_TAX_AMT_TRX_CURR,
                            jtl.UNROUND_TAX_AMT_TAX_CURR,
                            jtl.UNROUND_TAX_AMT_FUN_CURR,
                            jtl.UNROUNDED_UOM_CONV_QTY,
                            jtl.ROUNDED_TAX_AMT_TRX_CURR,
                            jtl.ROUNDED_TAX_AMT_TAX_CURR,
                            jtl.ROUNDED_TAX_AMT_FUN_CURR,
                            jtl.ROUNDED_TAXABLE_AMT_TRX_CURR,
                            jtl.ROUNDED_TAXABLE_AMT_TAX_CURR,
                            jtl.ROUNDED_TAXABLE_AMT_FUN_CURR,
                            jtl.ROUNDED_QUANTITY,
                            jtl.REC_TAX_AMT_TRX_CURR,
                            jtl.REC_TAX_AMT_TAX_CURR,
                            jtl.REC_TAX_AMT_FUNCL_CURR,
                            jtl.NREC_TAX_AMT_TRX_CURR,
                            jtl.NREC_TAX_AMT_TAX_CURR,
                            jtl.NREC_TAX_AMT_FUNCL_CURR,
                            jtl.ORIGINAL_TAX_AMT,
                            jtl.ENCUMBERANCE_NR_TAX_AMT,
                            jtl.ENCUMBERANCE_NR_FUNC_TAX_AMT,
                            jtl.ENCUMBERANCE_STATUS_FLAG,
                            jtl.TAX_LINE_NUM,
                            jtl.PRECEDENCE_1,
                            jtl.PRECEDENCE_2,
                            jtl.PRECEDENCE_3,
                            jtl.PRECEDENCE_4,
                            jtl.PRECEDENCE_5,
                            jtl.PRECEDENCE_6,
                            jtl.PRECEDENCE_7,
                            jtl.PRECEDENCE_8,
                            jtl.PRECEDENCE_9,
                            jtl.PRECEDENCE_10,
                            jtl.PRECEDENCE_11,
                            jtl.PRECEDENCE_12,
                            jtl.PRECEDENCE_13,
                            jtl.PRECEDENCE_14,
                            jtl.PRECEDENCE_15,
                            jtl.PRECEDENCE_16,
                            jtl.PRECEDENCE_17,
                            jtl.PRECEDENCE_18,
                            jtl.PRECEDENCE_19,
                            jtl.PRECEDENCE_20,
                            NVL(jdf.TAX_INVOICE_DATE,jtl.TAX_INVOICE_DATE) TAX_INVOICE_DATE,
                            NVL(jdf.TAX_INVOICE_NUM,jtl.TAX_INVOICE_NUM) TAX_INVOICE_NUM,
                            jtl.RECORD_TYPE_CODE,
                            jtl.CREATION_DATE,
                            jtl.CREATED_BY,
                            jtl.LAST_UPDATE_DATE,
                            jtl.LAST_UPDATED_BY,
                            jtl.LAST_UPDATE_LOGIN,
                            jtl.OBJECT_VERSION_NUMBER,
                            jtl.USER_ENTERED_AV,
                            jtl.TAXABLE_BASIS,
                            jtl.EXEMPTION_TYPE,
                            jtl.EXEMPTION_NUM,
                            jtl.TRACKING_NUM,
                            jtl.EXEMPTION_DATE,
                            jtl.PROOF_OF_EXPORT_NUM,
                            jtl.PROOF_RECEIVED_DATE,
                            jtl.EXEMPTION_HDR_ID,
                            jtl.TAX_AMT_BEFORE_EXEMPTION,
                            NULL THIRD_PARTY_PROC_FLAG,
                            NULL TP_STANDARD_INV_NUM,
                            NULL TP_CREDIT_INV_NUM,
                            NULL RETROACTIVE_FLAG,
                            NULL STATUS,
                            NULL ELIGIBLE_GSTR1,
                            NULL STATUS_GSTR1,
                            NULL ELIGIBLE_GSTR2,
                            NULL STATUS_GSTR2,
                            NULL ERROR_MSG_GSTR1,
                            NULL ERROR_MSG_GSTR2,
                            NULL ERROR_MSG_CODE1,
                            NULL ERROR_MSG_CODE2,
                            NULL SOURCE,
                            NULL SOURCE_APPLICATION,
                            NULL PROCESS_STATUS,
                            NULL BATCH_ID,
                            (SELECT msi.description
                              FROM  mtl_system_items_b msi
                             WHERE 1=1
                               AND msi.organization_id   = jtl.organization_id
                               AND msi.inventory_item_id = jtl.item_id ) Item_description,
                            jtt.tax_type_code tax_type_code,
                            (SELECT NVL(extended_amount,0)
                              FROM ra_customer_trx_lines_all
                             WHERE customer_trx_id      = jtl.trx_id
                               AND customer_trx_line_id = jtl.TRX_LINE_ID
                             ) extended_amount,
                            jdf.TRX_UOM_CODE      def_TRX_UOM_CODE,
                            jdf.TRX_LINE_QUANTITY def_TRX_LINE_QUANTITY,
                            jdf.event_class_code  def_event_class_code,
                            (SELECT jtdf.trx_type
                            FROM jai_tax_det_fct_lines_v jtdf
                            WHERE jtl.det_factor_id  = jtdf.det_factor_id
                            )def_trx_type,
                            (SELECT SUM (NVL (liability_amount, 0))
                               FROM jai_rgm_recovery_lines jrrl
                              WHERE jrrl.event_class_code = 'SALES_TXN_ADJUSTMENT'
                                AND jrrl.entity_code like 'TRANS%'
                                AND jrrl.document_id = jtl.trx_id
                                AND jrrl.status      = 'CONFIRMED'
                                AND jrrl.TAX_LINE_ID = jtl.trx_line_id) liability_amount,
                            (SELECT reporting_code
                             FROM jai_reporting_codes
                             WHERE reporting_code_id = NVL(hsn_code_id,sac_code_id)
                            )  hsn_sc,
                            jdf.bill_to_state,
                            jdf.ship_to_state,
                            jdf.SHIP_FROM_STATE
                          FROM jai_tax_lines_v jtl,
                               jai_tax_types   jtt,
                               jai_tax_det_factors jdf
                         WHERE jtl.org_id IS NOT NULL
                          AND jtt.tax_type_id = jtl.tax_type_id
                          AND jtl.det_factor_id   = jdf.det_factor_id(+)
                          AND jtl.trx_id          = jdf.trx_id(+)
                          AND jtl.TRX_LINE_ID     = jdf.TRX_LINE_ID(+)
                          AND TO_CHAR(jtl.trx_date,'MMYYYY')  = p_period
                          AND NOT EXISTS (SELECT 1
                                             FROM XXATUIND_GSTR1_TAB xgt
                                            WHERE xgt.TAX_LINE_ID = jtl.TAX_LINE_ID
                                              AND TO_CHAR(xgt.trx_date,'MMYYYY')  = p_period
                                           )
                         ;
        COMMIT;
   EXCEPTION
   WHEN OTHERS THEN
       fnd_file.put_line(fnd_file.log,'Exception occured while calling insert_gstr1_proc '||SQLERRM);
   END insert_gstr1_proc;



PROCEDURE XML_RESPONSE_UPDATE_PROC(P_BATCH_ID IN NUMBER,
                                      P_PERIOD IN VARCHAR2,
                                      P_FIRST_PARTY_REG_NUM IN VARCHAR2,
                                      P_XML_RESP XMLTYPE)

 -- +=========================================================================
  /* Oracle Applications : R12                                                                                         *
  * I --> Initial                                                                                                     *
  * E --> Enhancement                                                                                                 *
  * R --> Requirement                                                                                                 *
  * B --> Bug                                                                                                         *
  *********************************************************************************************************************
  --$Header: XXATUIND_GSTR1_JSON_PKG.pkb 0.0.0.0  30-MAR-2018    $                                                      *
  /********************************************************************************************************************
  * Type                :  PROCEDURE                                                                               *
  * Procedures          :  XML_RESPONSE_UPDATE_PROC                                                                                         *
  *                                                                                                                   *
  * Functions           :                                                                                             *
  * Purpose             : PROCEDURE TO CALL GET RESPONSE AND UPDATE IN THE TABLE                                      *
  *                                                                                                                   *
  * Company             :                                                                                             *
  * Created By          : ROHIT PRASAD                                                                                *
  * Created Date        : 14-APR-2018                                                                                 *
  * Modified By         :                                                                                             *
  * Last Reviewed By    :                                                                                             *
  * Last Reviewed Date  :                                                                                             *
  *********************************************************************************************************************
  * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification ---------->*
  * Date        By               Script   MKS        By                Date        Type Details                       *
  * ----------- ---------------- -------- ---------- ----------------- ----------- ---- ----------------------------- *
  * 14-APR-2018     ROHIT PRASAD                                                      I    Initial Version
  -- +=========================================================================                                       */

   IS

   --LOCAL VARIABLES
   tot_count      NUMBER;
   process_countno  NUMBER;
   error_record   VARCHAR2(1000);
   error_message  VARCHAR2(1000);
   l_invoice_num  VARCHAR2(1000);
   x_path       VARCHAR2(1000);
   x_path1       VARCHAR2(1000);
   x_path2       VARCHAR2(1000);
   l_txval        NUMBER;
   errror_count        NUMBER;




   CURSOR ID_UPDATE
   IS
   SELECT
   DETAIL_TAX_LINE_ID
   FROM XXATUIND_GSTR1_MAIN_TAB
   WHERE PERIOD_NAME = P_PERIOD
   AND batch_id=P_BATCH_ID
   AND first_party_primary_reg_num=P_FIRST_PARTY_REG_NUM;


   BEGIN

   fnd_file.put_line(fnd_file.log,'inside XML_RESPONSE_UPDATE_PROC');
   FOR J IN ID_UPDATE
   LOOP



      tot_count:=NULL;
      process_countno:=NULL;

       errror_count      :=NULL;
      fnd_file.put_line(fnd_file.log,'before tot_count');

      SELECT extractvalue( P_XML_RESP,'/GSTR1OutputResponse/Actions/GSTR1Action/Details/GSTR1Detail/TotalRecordsCount')
      total_count  INTO tot_count
      from dual;

    fnd_file.put_line(fnd_file.log,'tot_count  ::: '||tot_count);

    SELECT extractvalue( P_XML_RESP,'/GSTR1OutputResponse/Actions/GSTR1Action/Details/GSTR1Detail/ProcessedRecordscount')
      process_count  INTO process_countno
      from dual;

      fnd_file.put_line(fnd_file.log,'process_countno  ::: '||process_countno);

      SELECT extractvalue( P_XML_RESP,'/GSTR1OutputResponse/Actions/GSTR1Action/Details/GSTR1Detail/ErrorRecordsCount')
      error_count  INTO errror_count
      from dual;

      fnd_file.put_line(fnd_file.log,'errror_count  ::: '||errror_count);

             IF tot_count=process_countno

             THEN
             UPDATE XXATUIND_GSTR1_MAIN_TAB xgmt
             SET ERROR_MSG_CODE1  = 'SUCCESS'
              WHERE DETAIL_TAX_LINE_ID  =        J.DETAIL_TAX_LINE_ID
              AND PERIOD_NAME      =             P_PERIOD
              AND batch_id         =             p_batch_id
              AND first_party_primary_reg_num =  P_FIRST_PARTY_REG_NUM;

             COMMIT;
             fnd_file.put_line(fnd_file.log,'update success first');
             ELSE

             FOR k IN 1..(errror_count)
              LOOP



       l_invoice_num:=NULL;
       l_txval      :=NULL;
       error_message      :=NULL;

    x_path:=   '/GSTR1OutputResponse/Actions/GSTR1Action/Details/GSTR1Detail/ErrorRecords/GSTR1ErrorRecord['||k||']/inum';
    x_path1:=   '/GSTR1OutputResponse/Actions/GSTR1Action/Details/GSTR1Detail/ErrorRecords/GSTR1ErrorRecord['||k||']/txval';
    x_path2:=   '/GSTR1OutputResponse/Actions/GSTR1Action/Details/GSTR1Detail/ErrorRecords/GSTR1ErrorRecord['||k||']/errormessage';

			 fnd_file.put_line(fnd_file.log,'before invoice');
              select extractvalue( P_XML_RESP,x_path)
              invoice_num  INTO l_invoice_num
              from dual ;

			  fnd_file.put_line(fnd_file.log,'l_invoice_num ::: '||l_invoice_num);
               select extractvalue( P_XML_RESP,x_path1)
              TXVAL  INTO l_txval
              from dual ;

			  fnd_file.put_line(fnd_file.log,'l_txval ::: '||l_txval);
              select extractvalue( P_XML_RESP,x_path2)
              error_msg  INTO error_message
              from dual ;

     fnd_file.put_line(fnd_file.log,'l_invoice_num'||l_invoice_num);
     fnd_file.put_line(fnd_file.log,'l_txval '||l_txval);
     fnd_file.put_line(fnd_file.log,'error_msg '||error_message);


        UPDATE XXATUIND_GSTR1_MAIN_TAB xgmt
             SET ERROR_MSG_CODE1  = 'ERROR',
                 ERROR_MSG_GSTR1=    error_message
           WHERE DETAIL_TAX_LINE_ID = J.DETAIL_TAX_LINE_ID
              AND PERIOD_NAME = P_PERIOD
              AND batch_id = p_batch_id
              AND NVL(taxable_amt,0)= NVL(l_txval,0)
              AND NVL(tax_invoice_number,'X') = NVL(REPLACE(l_invoice_num,'ATUTN','ATU/TN'),'X')
              AND first_party_primary_reg_num = P_FIRST_PARTY_REG_NUM;


            COMMIT;





   END LOOP;

   UPDATE XXATUIND_GSTR1_MAIN_TAB xgmt
             SET ERROR_MSG_CODE1  = 'SUCCESS'
           WHERE DETAIL_TAX_LINE_ID = J.DETAIL_TAX_LINE_ID
              AND PERIOD_NAME = P_PERIOD
              AND batch_id = p_batch_id
              AND ERROR_MSG_CODE1 IS NULL
              AND first_party_primary_reg_num = P_FIRST_PARTY_REG_NUM;

              COMMIT;

               END IF;

   END LOOP;





   COMMIT;

  fnd_file.put_line(fnd_file.log,'end of XML_RESPONSE_UPDATE_PROC');

   EXCEPTION
   WHEN OTHERS THEN
       fnd_file.put_line(fnd_file.log,'Exception occured while calling XML_RESPONSE_UPDATE_PROC '||SQLERRM);
   END XML_RESPONSE_UPDATE_PROC;


   PROCEDURE CALL_REPORT_PROC(p_request_id IN NUMBER)
AS
  -- +=========================================================================
  /* Oracle Applications : R12                                                                                        *
  * I --> Initial                                                                                                     *
  * E --> Enhancement                                                                                                 *
  * R --> Requirement                                                                                                 *
  * B --> Bug                                                                                                         *
  *********************************************************************************************************************
  --$Header: XXATUIND_GSTR1_JSON_PKG.pkb 0.0.0.0  30-MAR-2018    $                                                      *
  /********************************************************************************************************************
  * Type                :  PROCEDURE                                                                              *
  * Procedures          :  CALL_REPORT_PROC                                                                                         *
  *                                                                                                                   *
  * Functions           :                                                                                             *
  * Purpose             :  PROCEDURE TO CALL REPORT PROGRAM AFTER COMPLETION OF GSTR1 INTERFACE PROGRAM                                         *
  *                                                                                                                   *
  * Company             :                                                                                             *
  * Created By          : ROHIT PRASAD                                                                                *
  * Created Date        : 14-APR-2018                                                                                 *
  * Modified By         :                                                                                             *
  * Last Reviewed By    :                                                                                             *
  * Last Reviewed Date  :                                                                                             *
  *********************************************************************************************************************
  * <------- Modified ---------> <---- Version ----> <--------- Reviewed --------> <--------- Modification ---------->*
  * Date        By               Script   MKS        By                Date        Type Details                       *
  * ----------- ---------------- -------- ---------- ----------------- ----------- ---- ----------------------------- *
  * 14-APR-2018     ROHIT PRASAD                                                      I    Initial Version
  -- +=========================================================================                                       */

  lv_request_id1       NUMBER;
  lv_request_id       NUMBER;
  l_layout       BOOLEAN;
  lc_phase            VARCHAR2(50);
  lc_status           VARCHAR2(50);
  lc_dev_phase        VARCHAR2(50);
  lc_dev_status       VARCHAR2(50);
  lc_message          VARCHAR2(50);
  l_req_return_status BOOLEAN;
BEGIN
  --
  --Setting Context
  lv_request_id1:= p_request_id;
  --
   fnd_global.apps_initialize (
      user_id             => fnd_profile.VALUE ('USER_ID'),
      resp_id             => fnd_profile.VALUE ('RESP_ID'),
      resp_appl_id        => fnd_profile.VALUE ('RESP_APPL_ID'),
      security_group_id   => 0);
  --
  -- Submitting GSTR1 Interface Program;

  IF lv_request_id1 > 0 THEN
          --Submitting Second Concurrent Program GSTR1 Report Print Program



        l_layout :=   fnd_request.add_layout(
                            template_appl_name => 'AR',
                            template_code      => 'XXATUIND_GSTR1_INT_REP',
                             template_language  => 'en',
                            template_territory => 'US',
                            output_format      => 'EXCEL',
                            nls_language       =>   NULL);


        lv_request_id := fnd_request.submit_request (
                            application   => 'AR',
                            program       => 'XXATUIND_GSTR1_INT_REP',
                            description   =>  NULL,
                            start_time    => sysdate,
                            sub_request   => FALSE,
                            argument1     => p_request_id);
        --
        COMMIT;
        --

    ELSE
      fnd_file.put_line(fnd_file.log, 'The GSTR1  Interface Program request failed. Oracle request id: ' || lv_request_id ||' '||SQLERRM);
    END IF;
  -- END IF;
EXCEPTION
WHEN OTHERS THEN
  fnd_file.put_line(fnd_file.log, 'OTHERS exception while submitting GSTR1 Interface Program: ' || sqlerrm);
END CALL_REPORT_PROC;

END XXATUIND_GSTR1_JSON_PKG;
/