packagejavaAssignment; 
importjava.time.LocalDate;
importjava.time.format.DateTimeFormatter;
importjava.util.*;
public class GymApp
{
privatestatic finalScanner scanner=newScanner(System.in);
private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MMdd");
privatestaticfinalMap<Integer,Member>members=newHashMap<>();
privatestaticfinalMap<Integer,Trainer>trainers=newHashMap<>(); 
privatestaticfinalMap<Integer,Plan>plans=newHashMap<>();
privatestaticfinalMap<Integer,Subscription>subscriptions=newHashMap<>();
privatestaticfinalMap<Integer,Invoice>invoices=newHashMap<>();
privatestaticfinalMap<Integer,Payment>payments=newHashMap<>(); 
privatestaticfinalMap<Integer,List<AttendanceRecord>>attendance=new
HashMap<>();
publicstaticvoidmain(String[]args){
seedSampleData();
while (true){ 
printMenu();
intchoice=readInt("Chooseoption:",1,9);
switch(choice){
case1->addMember();
case2->addPlan();
case 3 -> startOrRenewSubscription();
case4->recordAttendance();
case 5 -> generateInvoice();
case 6 -> recordPayment();
case7->displayMembers();
case8->displayAttendanceSummary();
case 9 -> { System.out.println("Exiting. Goodbye!"); return; }
default ->System.out.println("Invalidoption.");
}
System.out.println();
}
}
privatestatic voidprintMenu(){
System.out.println("===GymMembership&BillingSystem===");
System.out.println("1.AddMember");
System.out.println("2.AddPlan"); 
System.out.println("3. Start or Renew Subscription");
System.out.println("4.RecordAttendance"); 
System.out.println("5.GenerateInvoice");
System.out.println("6.RecordPayment"); 
System.out.println("7. Display Members & Subscriptions");
System.out.println("8.DisplayAttendanceSummary"); 
System.out.println("9.Exit");
}
privatestaticvoidaddMember(){ 
System.out.println("AddMember:");
Stringname=readNonEmptyString("Name:"); 
Stringphone=readNonEmptyString("Phone:");
Stringemail=readNonEmptyString("Email:"); 
Memberm=newMember(name,phone,email);
members.put(m.getId(),m); 
System.out.println("Added:"+m);
}
privatestaticvoidaddPlan(){ 
System.out.println("AddPlan:");
Stringname=readNonEmptyString("Planname(e.g.,Monthly,Quarterly,Yearly):");
intdurationDays=readInt("Durationindays(e.g.,30formonthly):",1,3650); 
doubleprice=readDouble("Price(inINR):",0.0,1_000_000.0);
doublepenaltyPercent=readDouble("Laterenewalpenaltypercent(e.g.,10for10%):",
0.0,100.0);
Planp=newPlan(name,durationDays,price,penaltyPercent);
plans.put(p.getId(),p);
System.out.println("AddedPlan:"+p);
}
private static voidstartOrRenewSubscription() { 
System.out.println("Start or Renew Subscription:");
if(members.isEmpty()){System.out.println("Nomembersexist.Addmembersfirst.");
return;}
if(plans.isEmpty()){System.out.println("Noplansexist.Addplansfirst.");return;}
Member member =chooseMember();
Planplan=choosePlan();
Subscription existing = findActiveSubscriptionForMember(member);
LocalDatetoday=LocalDate.now();
if (existing==null||!existing.isActiveOn(today)){
booleanwasExpired=(existing!=null&&!existing.isActiveOn(today));
if(wasExpired){
System.out.println("Subscriptionwasexpired.Alatepenaltymayapplyper plan
policy.");
}
Subscriptionsub=newSubscription(member,plan,today); 
subscriptions.put(sub.getId(),sub); 
attendance.putIfAbsent(member.getId(),newArrayList<>()); 
System.out.printf("Subscriptionstartedformember%s.Validuntil%s%n",
member.getName(), sub.getEndDate().format(DF));
System.out.println("Note:Generateinvoicenext(menuoption5)beforerecording
payment.");
}else{
System.out.println("Memberhasanactivesubscription.Renewingwillextendfrom
existingenddate"+existing.getEndDate().format(DF));
existing.renew();
System.out.printf("Subscription renewed. New validity: %s -%s%n",
existing.getStartDate().format(DF),existing.getEndDate().format(DF));
}
}
privatestaticvoidrecordAttendance(){ 
System.out.println("RecordAttendance:");
if (members.isEmpty()) { System.out.println("No members."); return; }
Member member =chooseMember();
LocalDatedate=readDate("Attendancedate(yyyy-MM-dd)[leaveblankfortoday]:",
true);
if(date==null)date=LocalDate.now();
Subscription active = findActiveSubscriptionForMember(member);
if(active==null||!active.isActiveOn(date)){
System.out.println("Cannot record attendance: member does not have an active
subscriptionon"+date.format(DF));
return;
}
Stringnotes=readString("Notes(optional):");
AttendanceRecordar=newAttendanceRecord(member,date,notes); 
attendance.computeIfAbsent(member.getId(), k -> new ArrayList<>()).add(ar);
System.out.println("Attendancerecorded:" +ar);
}
privatestaticvoidgenerateInvoice(){ 
System.out.println("GenerateInvoice:");
if(subscriptions.isEmpty()){System.out.println("Nosubscriptionsexist.");return;}
Subscriptionsub=chooseSubscription();
Invoice inv = Invoice.createForSubscription(sub);
invoices.put(inv.getId(),inv); 
System.out.println("Invoicegenerated:"); 
System.out.println(inv.detailedString());
}
privatestaticvoidrecordPayment(){ 
System.out.println("RecordPayment:");
if(invoices.isEmpty()){System.out.println("Noinvoicesexist.Generateaninvoice
first."); return;}
Invoiceinvoice=chooseInvoice();
if(invoice.isPaid()){
System.out.println("Invoicealready paid.");
return;
}
double amountDue = invoice.getTotal() -invoice.getAmountPaid();
System.out.printf("Amountdue:%.2f%n",amountDue);
double amount = readDouble(String.format("Enter payment amount (<= %.2f): ",
amountDue),0.01,amountDue);
String method = readNonEmptyString("Payment method (Cash/Card/UPI): ");
Paymentp=newPayment(invoice,amount,LocalDate.now(),method); 
payments.put(p.getId(),p);
invoice.applyPayment(p); 
System.out.println("Paymentrecorded:"+p);
System.out.println("Updatedinvoicestatus:");
System.out.println(invoice.detailedString());
}
privatestaticvoiddisplayMembers(){ 
System.out.println("Members & Subscriptions:");
if (members.isEmpty()) { System.out.println("No members."); return; }
for(Memberm:members.values()){
System.out.println(m);
Subscription s = findActiveSubscriptionForMember(m);
if(s != null){
System.out.println(" Activesubscription:"+s.briefString());
}else{
System.out.println("Noactivesubscription.");
}
List<Invoice> invs = invoicesForMember(m);
if(!invs.isEmpty()){
System.out.println("Invoices:");
for (Invoiceinv:invs){
System.out.printf(" %s-Paid:%s-Total:%.2f%n",inv.briefString(),
inv.isPaid()?"YES":"NO",inv.getTotal());
}
}
}
}
privatestaticvoiddisplayAttendanceSummary() { 
System.out.println("Attendance Summary per Member:");
if(attendance.isEmpty()){System.out.println("Noattendancerecords.");return;}
for(Memberm:members.values()){
List<AttendanceRecord> list = attendance.getOrDefault(m.getId(),
Collections.emptyList());
if(list.isEmpty())continue;
System.out.printf("%s(%drecords):%n",m.getName(),list.size());
//showlast5entries
list.stream()
.sorted(Comparator.comparing(AttendanceRecord::getDate).reversed())
.limit(10)
.forEach(ar -> System.out.printf(" %s -%s%n", ar.getDate().format(DF),
ar.getNotes()));
}
}
privatestatic Member chooseMember() {
while (true){
System.out.println("Members:");
members.values().forEach(m -> System.out.printf(" %d) %s%n", m.getId(), m));
intid=readInt("Entermemberid:",Integer.MIN_VALUE,Integer.MAX_VALUE);
if(members.containsKey(id))returnmembers.get(id); 
System.out.println("Invalidmember id.Tryagain.");
}
}
privatestaticPlanchoosePlan(){
while (true){
System.out.println("Plans:");
plans.values().forEach(p->System.out.printf("%d)%s%n",p.getId(),p)); 
intid=readInt("Enterplanid:",Integer.MIN_VALUE,Integer.MAX_VALUE);
if (plans.containsKey(id)) return plans.get(id);
System.out.println("Invalidplanid.Tryagain.");
}
}
private static Subscription chooseSubscription() {
while (true){
System.out.println("Subscriptions:");
subscriptions.values().forEach(s -> System.out.printf(" %d) %s%n", s.getId(),
s.briefString()));
intid=readInt("Entersubscriptionid:",Integer.MIN_VALUE,
Integer.MAX_VALUE);
if (subscriptions.containsKey(id)) return subscriptions.get(id);
System.out.println("Invalidsubscriptionid.");
}
}
privatestaticInvoicechooseInvoice(){
while (true){
System.out.println("Invoices:");
invoices.values().forEach(i -> System.out.printf(" %d) %s%n", i.getId(),
i.briefString()));
intid=readInt("Enterinvoiceid:",Integer.MIN_VALUE,Integer.MAX_VALUE);
if(invoices.containsKey(id)) returninvoices.get(id); 
System.out.println("Invalidinvoiceid.");
}
}
private static Subscription findActiveSubscriptionForMember(Member m) {
LocalDatetoday=LocalDate.now();
returnsubscriptions.values().stream()
.filter(s->s.getMember().equals(m))
.max(Comparator.comparing(Subscription::getEndDate))
.filter(s->s.isActiveOn(today))
.orElse(null);
}
private static List<Invoice>invoicesForMember(Member m) {
List<Invoice>list=newArrayList<>();
for (Invoice inv: invoices.values()){
if (inv.getSubscription().getMember().equals(m)) list.add(inv);
}
list.sort(Comparator.comparing(Invoice::getCreatedAt).reversed());
returnlist;
}
privatestaticintreadInt(Stringprompt,intmin,intmax){
while (true){
System.out.print(prompt);
Stringline=scanner.nextLine().trim();
try{
intv=Integer.parseInt(line);
if(v<min|| v>max){
System.out.printf("Enter value between %d and %d%n", min, max);
continue;
}
returnv;
} catch(NumberFormatExceptione){ 
System.out.println("Invalid integer. Try again.");
}
}
}
privatestaticdoublereadDouble(Stringprompt,doublemin,doublemax){
while (true){
System.out.print(prompt);
Stringline=scanner.nextLine().trim();
try{
double v= Double.parseDouble(line);
if(v<min|| v>max){
System.out.printf("Entervaluebetween%.2fand%.2f%n",min,max);
continue;
}
returnv;
} catch(NumberFormatExceptione){ 
System.out.println("Invalid decimal number. Try again.");
}
}
}
privatestaticStringreadNonEmptyString(Stringprompt){
while (true){
System.out.print(prompt);
Strings=scanner.nextLine().trim();
if(!s.isEmpty()) returns;
System.out.println("Valuecannotbeempty.");
}
}
privatestaticStringreadString(Stringprompt){
System.out.print(prompt);
returnscanner.nextLine().trim();
}
private static String readNonEmptyName(String prompt){
returnreadNonEmptyString(prompt);
}
privatestaticLocalDatereadDate(Stringprompt,booleanallowBlank){
while (true){
System.out.print(prompt);
Strings=scanner.nextLine().trim();
if(s.isEmpty()&&allowBlank)returnnull;
try{
returnLocalDate.parse(s,DF);
} catch(Exception e){
System.out.println("Invaliddateformat.Useyyyy-MM-dd.");
}
}
}
privatestaticvoidseedSampleData(){
Planmonthly=newPlan("Monthly",30,1200.0,10.0);
Planquarterly=newPlan("Quarterly",90,3300.0,8.0);
Planyearly=newPlan("Yearly",365,12000.0,5.0);
plans.put(monthly.getId(),monthly); 
plans.put(quarterly.getId(),quarterly); 
plans.put(yearly.getId(),yearly);
Trainert=newTrainer("Ana","9876543210");
trainers.put(t.getId(),t);
Memberm1=newMember("SitaRam","9000000001","sita@example.com"); 
Member m2 = new Member("Rahul Kumar", "9000000002", "rahul@example.com");
members.put(m1.getId(),m1);
members.put(m2.getId(), m2);
Subscription s1 = new Subscription(m1, monthly, LocalDate.now().minusDays(5)); //
started5daysago
subscriptions.put(s1.getId(),s1); 
attendance.put(m1.getId(),newArrayList<>());
}
staticclassMember{
private static int counter = 1;
privatefinalintid;
private String name;
private String phone;
privateStringemail;
public Member(String name, String phone, String email) { 
this.id=counter++;
this.name=name; 
this.phone=phone;
this.email=email;
}
publicintgetId(){returnid;}
publicStringgetName(){returnname;} 
publicStringgetPhone(){returnphone;}
publicStringgetEmail(){returnemail;}
publicvoidsetName(Stringname){this.name=name;} 
publicvoidsetPhone(Stringphone){this.phone=phone;}
publicvoidsetEmail(Stringemail){this.email=email;} 
publicStringtoString(){
returnString.format("%s(ID:%d,%s,%s)",name,id,phone,email);
}
publicbooleanequals(Objecto){
if(!(oinstanceofMember))returnfalse;
return((Member)o).id==this.id;
}
publicinthashCode() { returnObjects.hash(id);}
}
static class Trainer {
privatestaticintcounter=1;
privatefinalintid;
privateStringname; 
privateStringphone;
publicTrainer(Stringname,Stringphone){
this.id=counter++;
this.name=name; 
this.phone=phone;
}
publicintgetId(){returnid;}
publicStringgetName() {returnname;}
publicStringtoString(){
returnString.format("%s(ID:%d,%s)",name,id,phone);
}
}
staticclassPlan{
privatestaticintcounter=1;
privatefinalintid;
privateStringname; 
privateint durationDays;
privatedoubleprice;
privatedoublepenaltyPercent;
publicPlan(Stringname,intdurationDays,doubleprice,doublepenaltyPercent){
this.id=counter++;
this.name=name; 
this.durationDays =durationDays;
this.price=price;
this.penaltyPercent=penaltyPercent;
}
publicintgetId(){returnid;}
publicStringgetName() {returnname;}
publicintgetDurationDays(){returndurationDays;}
publicdoublegetPrice(){returnprice;}
publicdoublegetPenaltyPercent(){returnpenaltyPercent;}
publicStringtoString(){
returnString.format("%s(ID:%d)-%ddays-Price:%.2f-Penalty:%.1f%%",name,
id,durationDays,price,penaltyPercent);
}
}
staticclassSubscription{ 
privatestaticintcounter=1;
privatefinalintid;
privatefinalMembermember;
privatefinalPlanplan;
privateLocalDatestartDate;
privateLocalDateendDate; 
privatebooleanactive;
public Subscription(Member member, Plan plan, LocalDate startDate) {
this.id=counter++;
this.member=member; 
this.plan=plan; 
this.startDate=startDate;
this.endDate = startDate.plusDays(plan.getDurationDays() -1);
this.active=true;
}
publicintgetId(){returnid;}
publicMembergetMember(){returnmember;}
publicPlangetPlan(){returnplan;}
publicLocalDategetStartDate(){returnstartDate;}
publicLocalDategetEndDate(){returnendDate;} 
publicbooleanisActive(){returnactive;}
publicbooleanisActiveOn(LocalDatedate){
12
return(date!=null) &&!date.isBefore(startDate) &&!date.isAfter(endDate);
}
publicvoidrenew() {
if(isActiveOn(LocalDate.now())){
LocalDatenewStart=endDate.plusDays(1);
LocalDate newEnd = newStart.plusDays(plan.getDurationDays() -1);
this.endDate=newEnd;
}else{
this.startDate=LocalDate.now();
this.endDate = startDate.plusDays(plan.getDurationDays() -1);
this.active=true;
}
}
publicStringbriefString(){
return String.format("Subscription[id=%d, plan=%s, member=%s, %s -%s]", id,
plan.getName(), member.getName(), startDate.format(DF), endDate.format(DF));
}
publicStringtoString(){
returnbriefString();
}
}
static class AttendanceRecord {
private static int counter = 1; 
privatefinalintid;
privatefinalMembermember;
privatefinalLocalDatedate; 
privatefinalStringnotes;
public AttendanceRecord(Member member, LocalDate date, String notes) {
this.id=counter++;
this.member=member;
this.date=date;
this.notes = (notes== null||notes.isEmpty())?"-": notes;
}
publicintgetId(){returnid;}
publicMembergetMember(){returnmember;}
publicLocalDategetDate(){returndate;} 
publicStringgetNotes(){returnnotes;}
publicStringtoString(){
return String.format("Attendance[id=%d, member=%s, date=%s, notes=%s]", id,
member.getName(),date.format(DF),notes);
}
}
staticclassInvoice{
privatestaticintcounter=1;
privatefinalintid;
privatefinal Subscriptionsubscription;
privatefinalLocalDatecreatedAt;
privatefinaldoublesubtotal; 
privatefinaldoubletaxPercent; 
privatefinaldoublepenalty; 
privatedoubleamountPaid;
privatestaticfinaldoubleDEFAULT_TAX_PERCENT=18.0;
private Invoice(Subscription subscription, double subtotal, double penalty, double
taxPercent){
this.id=counter++; 
this.subscription=subscription; 
this.createdAt =LocalDate.now();
this.subtotal=subtotal; 
this.penalty=penalty; 
this.taxPercent=taxPercent; 
this.amountPaid=0.0;
}
public static Invoice createForSubscription(Subscription s) {
doubleprice=s.getPlan().getPrice();
doublepenalty=0.0;
LocalDatetoday=LocalDate.now(); 
boolean expired = !s.isActiveOn(today);
if(expired){
penalty=price*(s.getPlan().getPenaltyPercent()/100.0);
}
doublesubtotal=price;
returnnewInvoice(s, subtotal,penalty,DEFAULT_TAX_PERCENT);
}
publicintgetId(){returnid;}
publicSubscriptiongetSubscription(){returnsubscription;}
publicLocalDategetCreatedAt(){returncreatedAt;}
publicdoublegetSubtotal(){returnsubtotal;} 
publicdoublegetTaxPercent(){returntaxPercent;}
publicdoublegetPenalty(){returnpenalty;}
publicdoublegetAmountPaid(){returnamountPaid;}
publicdoublegetTaxAmount(){
return(subtotal+penalty) * taxPercent/100.0;
}
publicdoublegetTotal(){
returnsubtotal+penalty + getTaxAmount();
}
publicdoublegetAmountDue(){ 
return getTotal() -amountPaid;
}
publicbooleanisPaid() {
returnMath.abs(getAmountDue())<0.0001;
}
publicvoid applyPayment(Payment p) {
this.amountPaid+=p.getAmount();
}
publicStringbriefString(){
return String.format("Invoice[id=%d, member=%s, plan=%s, total=%.2f, paid=%.2f,
created=%s]", id, subscription.getMember().getName(), subscription.getPlan().getName(),
getTotal(),amountPaid, createdAt.format(DF));
}
publicStringdetailedString(){
returnString.format(
"InvoiceID:%d%nMember:%s%nPlan:%s%nCycle:%s-%s%nSubtotal:
%.2f%nPenalty:%.2f%nTax (%.1f%%):%.2f%nTotal:%.2f%nAmountPaid:%.2f%nStatus:
%s",
id, 
subscription.getMember().getName(),
subscription.getPlan().getName(),
subscription.getStartDate().format(DF),subscription.getEndDate().format(DF),
subtotal,
penalty,
taxPercent, 
getTaxAmount(),
getTotal(), 
amountPaid,
isPaid()?"PAID":"UNPAID"
);
}
publicStringtoString(){
returnbriefString();
}
}
staticclassPayment{
private static int counter = 1; 
privatefinalintid;
private final Invoice invoice;
private final double amount;
private final LocalDate date;
privatefinalStringmethod;
public Payment(Invoice invoice, double amount, LocalDate date, String method) {
this.id=counter++;
this.invoice=invoice; 
this.amount=amount;
this.date=date; 
this.method=method;
}
publicintgetId(){returnid;}
public InvoicegetInvoice(){returninvoice;}
publicdoublegetAmount(){returnamount;}
publicLocalDategetDate(){returndate;} 
publicStringgetMethod(){returnmethod;} 
publicStringtoString(){
return String.format("Payment[id=%d, invoiceId=%d, amount=%.2f, date=%s,
method=%s]",id,invoice.getId(),amount,date.format(DF),method);
}
}
}

OUTPUT:

GymApp [Java Application] C:\Users\HARINI,p2\pool\plugins\org.eclipse.justj.openjdkho

Gym Membership & Billing System

1. Add Member

2. Add Plan

3. Start or Renew Subscription

4. Record Attendance

5. Generate Invoice

6. Record Payment

7. Display Members & Subscriptions

8. Display Attendance Summary

9. Exit

Choose option: 1

Add Member:

Name: HARINI

Phone: 6589354760

Email: GSVFJS090

Added: HARINI (ID:3, 6589354760, GSVFJS@90)

Gym Membership & Billing System

1. Add Member

2. Add Plan

3. Start or Renew Subscription

4. Record Attendance

5. Generate Invoice

6. Record Payment

7. Display Members & Subscriptions

8. Display Attendance Summary

9. Exit

Choose option: 2

Add Plan:

Plan name (e.g., Monthly, Quarterly, Yearly): MONTLY

Duration in days (e.g., 30 for monthly):

Invalid integer. Try again.

Duration in days (e.g., 30 for monthly): 28

Price (in INR): 5600

Late renewal penalty percent (e.g., 10 for 10%): 800

Enter value between 0.00 and 100.00

Late renewal penalty percent (e.g., 10 for 10%): 100.00

Added Plan: MONTLY (ID:4) 28 days Price: 5600.00 Penalty: 100.0%

Gym Membership & Billing System 1. Add Member
