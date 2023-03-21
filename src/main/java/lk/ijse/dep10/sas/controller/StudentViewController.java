package lk.ijse.dep10.sas.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.sas.DB.DBConnection;
import lk.ijse.dep10.sas.model.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentViewController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPicture;

    @FXML
    private TableView<Student> tblStudents;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearch;
    public void initialize(){
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("picture"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));

    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the student picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.jpg","*jpeg","*.png","*gif","*bmp"));
        File file=fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());

        if (file !=null) {
            Image image = new Image(file.toURI().toURL().toString(), 200.0, 172.0, true, true);
            imgPicture.setImage(image);
            btnClear.setDisable(false);
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        imgPicture.setImage(new Image("/image/no-image.png"));
        btnClear.setDisable(true);

    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Student selectedStudent = tblStudents.getSelectionModel().getSelectedItem();
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement statement1 = connection.prepareStatement("DELETE FROM Picture WHERE student_id = ?");
            statement1.setString(1, selectedStudent.getId());
            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM Student WHERE id = ?");
            statement2.setString(1, selectedStudent.getId());

            statement1.executeUpdate();
            statement2.executeUpdate();

            tblStudents.getItems().remove(tblStudents.getSelectionModel().getSelectedIndex());
            btnNewStudent.fire();
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        System.out.println(tblStudents.getItems());
        String newStudentId = "Dep10/S001";
        if (tblStudents.getItems() != null) {
            String lastStudentId = (tblStudents.getItems().get(tblStudents.getItems().size() - 1).getId().substring(8));
            newStudentId = String.format("Dep10/S%03d", Integer.parseInt(lastStudentId) + 1);
        }

        txtId.setText(newStudentId);
        txtName.clear();
//        btnClear.fire();
        imgPicture.setImage(new Image("/image/empty-photo.png"));
        tblStudents.getSelectionModel().clearSelection();


    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!txtName.getText().strip().matches("[A-z ]+")){
            txtName.selectAll();
            txtName.requestFocus();
        }
        System.out.println("DataValid");

        Student newStudent = new Student(txtId.getText(), txtName.getText(), null);
        tblStudents.getItems().add(newStudent);
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Student(id, name) VALUES (?,?)");
            PreparedStatement preparedStatementPicture = connection.prepareStatement("INSERT INTO Picture(student_id, picture) VALUES (?,?)");

            preparedStatement.setString(1,txtId.getText());
            preparedStatement.setString(2,txtName.getText());
            preparedStatement.executeUpdate();

            preparedStatementPicture.setString(1,txtId.getText());

            Image image = imgPicture.getImage();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", bos);
            byte[] bytes = bos.toByteArray();
            Blob picture = new SerialBlob(bytes);
            preparedStatementPicture.setBlob(2,picture);
            preparedStatementPicture.executeUpdate();
            newStudent.setPicture(picture);
            btnNewStudent.fire();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
        } catch (IOException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.printStackTrace();
            }
            throw new RuntimeException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    @FXML
    void tblStudentsOnKeyReleased(KeyEvent event) {

    }

}
