package com.getconfig.Document

import com.getconfig.Model.PortList
import com.getconfig.Model.RedmineParameter
import com.getconfig.Model.Ticket
import com.getconfig.Utils.TomlUtils
import com.taskadapter.redmineapi.Include
import com.taskadapter.redmineapi.IssueManager
import com.taskadapter.redmineapi.NotFoundException
import com.taskadapter.redmineapi.ProjectManager
import com.taskadapter.redmineapi.RedmineManager
import com.taskadapter.redmineapi.RedmineManagerFactory
import com.taskadapter.redmineapi.RedmineProcessingException
import com.taskadapter.redmineapi.bean.Issue
import com.taskadapter.redmineapi.bean.IssueFactory
import com.taskadapter.redmineapi.bean.Project
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

// setenv REDMINE_URL=http://localhost/redmine
// setenv REDMINE_API_KEY={APIキー}
// gradle --daemon test --tests "TestRedmineRepository.*"

/*
Redmine Java API 調査

* チケット作成の場合

  def new_issue = issue_manager.createIssue(issue); // 引数をリファレンスに発番

* チケット更新の場合

  issue_manager.update(issue); // issue を更新

更新の場合は名前でカスタムフィールドの指定ができるが、
作成の場合はカスタムフィールドID、名前、値の指定で登録が必要。

* カスタムフィールドの検索(Redmine 全体)

  def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()

* チケットの検索

  def params = new HashMap<String,String>();
  params.put("status_id","*");
  params.put("subject", subject);
  def results = issue_manager.getIssues(params).getResults()

* チケットのカスタムフィールド更新(新規チケットの場合)

  かなり複雑。一旦カスタムフィールドなしのチケットを作成してから次の更新をした方が良い

  # 事前にカスタムフィールドの辞書作成
  def custom_field_ids = [:]
  def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()
  custom_fields.each { custom_field ->
      custom_field_ids[custom_field.name] = custom_field.id
  }
  # 辞書からフィールドidをつき合わせし、カスタムフィールドを登録
  issue.addCustomField(CustomFieldFactory.create(custom_field_ids['OS名'], 'OS名', 'CentOS 6.10'))
  def new_issue = issue_manager.createIssue(issue);

* チケットのカスタムフィールド更新(既存チケットの場合)

  def custom_field = issue.getCustomFieldByName("OS名")
  custom_field.setValue('CentOS 6.9')
  issue_manager.update(issue);

* チケット削除

  mgr.getIssueManager().deleteIssue(0)

* リレーション登録

  def relations =[]
  def relation = issue_manager.createRelation(new_issue.id, issue_to.id, 'relates')
  relations << relation
  new_issue.addRelations(relations)
  issue_manager.update(new_issue)

* リレーション検索

  def relations = issue2.getRelations()

*/

@TypeChecked
@CompileStatic
@Slf4j
class TicketManager implements Controller {
    String redmineConfigPath = 'lib/dictionary/redmine.toml'
    String redmineUrl
    String redmineApiKey
    String redmineProject
    boolean dryRun

    RedmineParameter redmineParam
    RedmineManager redmineManager
    IssueManager issueManager
    ProjectManager projectManager

    TicketManager(String redmineConfigPath = null) {
        if (redmineConfigPath) {
            this.redmineConfigPath = redmineConfigPath
        }
    }

    void setEnvironment(ConfigEnv env) {
        this.redmineUrl = env.getRedmineUrl()
        this.redmineApiKey = env.getRedmineApiKey()
        this.redmineConfigPath = env.getRedmineConfigPath()
        this.redmineProject = env.getRedmineProject()
        this.dryRun = env.getDryRun()
    }

    void readConfig() throws IOException {
        redmineParam = TomlUtils.read(
            this.redmineConfigPath, RedmineParameter)as RedmineParameter
    }

    def init() {
        if (!this.redmineManager) {
            this.redmineManager = RedmineManagerFactory.createWithApiKey(
                    this.redmineUrl,
                    this.redmineApiKey)
        }
        if (!this.issueManager) {
            this.issueManager   = this.redmineManager.getIssueManager()
        }
        if (!this.projectManager) {
            this.projectManager = this.redmineManager.getProjectManager()
        }
    }

    Issue getIssue(String subject) {
        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);
        def results = this.issueManager.getIssues(params).getResults()
        // getIssues()だと、リレーションの検索が出来ないため、再度getIssueById()で検索する
        return (results.isEmpty()) ? null :
                this.issueManager.getIssueById(results[0].id,
                        Include.relations)
    }

    Project getProject(String projectName) {
        try {
            return this.projectManager.getProjectByKey(projectName)
        } catch (NotFoundException e) {
            def msg = "Not found Redmine project '${projectName}' : ${e}"
            log.error(msg)
            throw new NotFoundException(msg)
        }
    }

    def delete(String subject) {
        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);

        def issues = this.issueManager.getIssues(params);
        issues.getResults().each { issue ->
            this.issueManager.deleteIssue(issue.id)
        }
    }

    Issue resisterTicket(String projectName, String trackerName, String subject,
                         Map<String,String> customFields = null, Boolean inOperation = false) {
        Issue issue = null
        try {
            def project = this.getProject(projectName)
            def tracker = project.getTrackerByName(trackerName)
            if (!tracker) {
                def msg = "Not found Redmine tracker '${trackerName}' in '${projectName}'"
                log.error(msg)
                throw new NotFoundException(msg)
            }
            issue = getIssue(subject)
            if (!issue) {
                def newIssue = IssueFactory.create(null)
                newIssue.setProjectId(project.id)  // プロジェクト
                newIssue.setSubject(subject)       // 題名
                newIssue.setTracker(tracker)       // トラッカー
                issue = this.issueManager.createIssue(newIssue)
            }
            issue.setProjectId(project.id)
            issue.setSubject(subject)
            issue.setTracker(tracker)
            if (inOperation) {
                issue.setStatusId(redmineParam.inOperationStatusId())
            }
            updateCustomFields(issue, customFields)
        } catch (NotFoundException e) {
            def msg = "register '${trackerName}:${subject}'." + e
            log.error(msg)
            issue = null
        }
        return issue
    }

    void updateCustomField(Issue issue, String fieldName, String value,
                           boolean ignoreError = true) {
        def customField = issue.getCustomFieldByName(fieldName)
        if (!customField && !ignoreError) {
            def msg = "not found custom field '${fieldName}'"
            log.error(msg)
            throw new NotFoundException(msg)
        }
        if (customField) {
            customField.setValue(value)
        }
    }

    def updateCustomFields(Issue issue, Map<String, String> customFields = null) {
        def trackerName = issue.getTracker().getName()
        def subject  = issue.getSubject()
        customFields.each { String fieldName, String value ->
            updateCustomField(issue, fieldName, value, false)
        }
        updateCustomField(issue, redmineParam.inventoryField(), subject)
        updateCustomField(issue, redmineParam.rackLocationField(),
            redmineParam.rackLocation(subject))
        try {
            this.issueManager.update(issue)
        } catch (RedmineProcessingException e) {
            def msg = "update '${trackerName}:${subject}' set to '${customFields}' : ${e}."
            log.error(msg)
            throw new NotFoundException(msg)
        } catch (IllegalArgumentException e) {
            def msg = "https://github.com/taskadapter/redmine-java-api/issues : ${e}."
            log.warn(msg)
        }
        log.info "resister(#${issue.id}) '${trackerName}:${subject}'"
    }

    Map<String,String> getPortListCustomFields(Map<String, String> customFields) {
        Map<String,String> fields = new LinkedHashMap<>()
        redmineParam.custom_fields.each { String testItemName, String redmineName ->
            if (customFields.containsKey(testItemName)) {
                def value = customFields[testItemName]
                // println "PORT_LIST_CUSTOM_FIELDS:${test_item_name},${value}"
                if (value != null) {
                    fields[redmineName] = value
                }
            }
        }
        return fields
    }

    Boolean checkLookedUpPortList(Map<String, String> customFields) {
        return (customFields.containsKey('lookup') && customFields['lookup'] == true)
    }

    Issue resisterPortList(String projectName, String subject, Map customFields = [:]) {
        if (redmineParam.custom_fields.size() == 0) {
            def msg = "not found 'port_list_custom_fields' parameter"
            log.error(msg)
            throw new NotFoundException(msg)
        }
        def fields = this.getPortListCustomFields(customFields)
        def lookedUp = this.checkLookedUpPortList(customFields)
        return this.resisterTicket(projectName, redmineParam.portListTracker(),
                subject, fields, lookedUp)
    }

    Boolean link(Issue ticket_from, List<Integer> ticket_to_ids) {
        Boolean isok = false
        // 関連するチケットIDの洗い出し
        def existing_relations = [:]
        ticket_from.getRelations().each { issue ->
            existing_relations[issue.issueId] = true
            existing_relations[issue.issueToId] = true
        }
        // println "EXISTING_RELATIONS:${existing_relations}"
        def relations =[]
        try {
            ticket_to_ids.each { ticket_to_id ->
                if (!existing_relations.containsKey(ticket_to_id)) {
                    relations << this.issueManager.createRelation(ticket_from.id,
                            ticket_to_id,
                            'relates')
                }
            }
            // println "RELATIONS:${relations}"
            if (relations.size() > 0) {
                ticket_from.addRelations(relations)
                try {
                    this.issueManager.update(ticket_from)
                } catch (IllegalArgumentException e) {
                    def msg = "https://github.com/taskadapter/redmine-java-api/issues : ${e}."
                    log.warn(msg)
                }
            }
            isok = true
        } catch (RedmineProcessingException e) {
            def msg = "link error '${ticket_from}' to '${ticket_to_ids}' : ${e}."
            log.info(msg)
        }
        return isok
    }

    void resister(Ticket ticket) {
        if (this.dryRun) {
            println "register ${ticket}"
            return
        }
        Issue issue = resisterTicket(this.redmineProject, ticket.tracker,
            ticket.subject, ticket.custom_fields)
        List<Integer> portListIds = new ArrayList<>()
        ticket.port_lists.each { PortList portList ->
            Issue portListIssue
            portListIssue = resisterPortList(this.redmineProject,
                    portList.ip,
                    portList.customFields())
            if (portListIssue) {
                portListIds.add(portListIssue.id)
            }
        }
        if (portListIds.size() > 0) {
            link(issue, portListIds)
        }
    }

    void showConfig() {
        println "redmine loader config"
        println "    url     : ${redmineUrl}"
        println "    api key : ${redmineApiKey}"
        println "    project : ${redmineProject}"
    }

    int run() {
        return 0
    }

}
