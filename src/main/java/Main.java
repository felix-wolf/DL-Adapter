public class Main {

    public static void main(String[] args) {
        System.out.println("start");
        //new LogFileReader();
        try {
            KafkaProducerExample.runProducer(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
