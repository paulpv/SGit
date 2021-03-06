package me.sheimi.sgit.activities.delegate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.activities.delegate.actions.AddAllAction;
import me.sheimi.sgit.activities.delegate.actions.AddRemoteAction;
import me.sheimi.sgit.activities.delegate.actions.CherryPickAction;
import me.sheimi.sgit.activities.delegate.actions.CommitAction;
import me.sheimi.sgit.activities.delegate.actions.DeleteAction;
import me.sheimi.sgit.activities.delegate.actions.DiffAction;
import me.sheimi.sgit.activities.delegate.actions.MergeAction;
import me.sheimi.sgit.activities.delegate.actions.NewDirAction;
import me.sheimi.sgit.activities.delegate.actions.NewFileAction;
import me.sheimi.sgit.activities.delegate.actions.PullAction;
import me.sheimi.sgit.activities.delegate.actions.PushAction;
import me.sheimi.sgit.activities.delegate.actions.RebaseAction;
import me.sheimi.sgit.activities.delegate.actions.RepoAction;
import me.sheimi.sgit.activities.delegate.actions.ResetAction;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.AddToStageTask;
import me.sheimi.sgit.repo.tasks.repo.CheckoutFileTask;
import me.sheimi.sgit.repo.tasks.repo.CheckoutTask;
import me.sheimi.sgit.repo.tasks.repo.DeleteFileFromRepoTask;
import me.sheimi.sgit.repo.tasks.repo.MergeTask;

import org.eclipse.jgit.lib.Ref;

public class RepoOperationDelegate {

    private Repo mRepo;
    private RepoDetailActivity mActivity;
    private Map<String, RepoAction> mActions = new HashMap<String, RepoAction>();

    public RepoOperationDelegate(Repo repo, RepoDetailActivity activity) {
        mRepo = repo;
        mActivity = activity;
        initActions();
    }

    private void initActions() {
        mActions.put("Commit", new CommitAction(mRepo, mActivity));
        mActions.put("Delete", new DeleteAction(mRepo, mActivity));
        mActions.put("Diff", new DiffAction(mRepo, mActivity));
        mActions.put("Merge", new MergeAction(mRepo, mActivity));
        mActions.put("New File", new NewFileAction(mRepo, mActivity));
        mActions.put("New Directory", new NewDirAction(mRepo, mActivity));
        mActions.put("Pull", new PullAction(mRepo, mActivity));
        mActions.put("Push", new PushAction(mRepo, mActivity));
        mActions.put("Reset", new ResetAction(mRepo, mActivity));
        mActions.put("Add all to stage", new AddAllAction(mRepo, mActivity));
        mActions.put("Cherry Pick", new CherryPickAction(mRepo, mActivity));
        mActions.put("Rebase", new RebaseAction(mRepo, mActivity));
        mActions.put("Add Remote", new AddRemoteAction(mRepo, mActivity));
    }

    public void executeAction(String key) {
        RepoAction action = mActions.get(key);
        if (action == null)
            return;
        action.execute();
    }

    public void checkoutCommit(final String commitName) {
        CheckoutTask checkoutTask = new CheckoutTask(mRepo, commitName,
                new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        mActivity.reset(commitName);
                    }
                });
        checkoutTask.executeTask();
    }

    public void mergeBranch(final Ref commit, final String ffModeStr,
            final boolean autoCommit) {
        MergeTask mergeTask = new MergeTask(mRepo, commit, ffModeStr,
                autoCommit, new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        mActivity.reset();
                    }
                });
        mergeTask.executeTask();
    }

    public void addToStage(String filepath) {
        String relative = getRelativePath(filepath);
        AddToStageTask addToStageTask = new AddToStageTask(mRepo, relative);
        addToStageTask.executeTask();
    }

    public void checkoutFile(String filepath) {
        String relative = getRelativePath(filepath);
        CheckoutFileTask task = new CheckoutFileTask(mRepo, relative, null);
        task.executeTask();
    }

    public void deleteFileFromRepo(String filepath) {
        String relative = getRelativePath(filepath);
        DeleteFileFromRepoTask task = new DeleteFileFromRepoTask(mRepo,
                relative, new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        // TODO Auto-generated method stub
                        mActivity.getFilesFragment().reset();
                    }
                });
        task.executeTask();
    }

    private String getRelativePath(String filepath) {
        File base = mRepo.getDir();
        String relative = FsUtils.getRelativePath(new File(filepath), base);
        return relative;
    }

}
